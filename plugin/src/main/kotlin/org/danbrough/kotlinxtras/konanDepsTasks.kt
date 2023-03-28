package org.danbrough.kotlinxtras

import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget


val KonanTarget.konanDepsTaskName: String
  get() = "xtrasKonanDeps${platformName.capitalized()}"


internal fun Project.registerKonanDepsTasks(target: KonanTarget) {

  if (project.tasks.findByName(target.konanDepsTaskName) != null) return

  val depsProjectDir =
    rootProject.buildDir.resolve(".konandeps/xtraKonanDeps${target.platformName.capitalize()}")
  val projectTaskName = "xtrasKonanDepsProject${target.platformName.capitalized()}"

  project.tasks.register(projectTaskName) {

    outputs.dir(depsProjectDir)
    doFirst {
      depsProjectDir.mkdirs()
      depsProjectDir.resolve("gradle.properties").writeText(
        """
        kotlin.native.ignoreDisabledTargets=true
        org.gradle.parallel=false
        org.gradle.unsafe.configuration-cache=false
        
      """.trimIndent()
      )


      depsProjectDir.resolve("settings.gradle.kts").also {
        if (!it.exists()) it.createNewFile()
      }

      depsProjectDir.resolve("build.gradle.kts").writeText(
        """
          plugins {
            kotlin("multiplatform") version "${KotlinVersion.CURRENT}"
          }
          
          kotlin {
           ${target.platformName}()
          }
            
          repositories {
            mavenCentral()
          }

      """.trimIndent()
      )


      depsProjectDir.resolve("src/commonMain/kotlin").apply {
        mkdirs()
        resolve("test.kt").writeText(
          """
              fun test(){
                println("some code to compile")
              }
           """.trimIndent()
        )
      }
    }
  }

  project.tasks.register(
    target.konanDepsTaskName, GradleBuild::class.java
  ) {
    dependsOn(projectTaskName)
    dir = depsProjectDir
    tasks = listOf("compileKotlin${target.platformName.capitalized()}")
    doFirst {
      project.log("$name: running compileKotlin${target.platformName.capitalized()}")
    }
  }

}

