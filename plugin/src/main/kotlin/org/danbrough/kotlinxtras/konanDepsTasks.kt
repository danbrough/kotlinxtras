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
    rootProject.layout.buildDirectory.dir(".konandeps/xtraKonanDeps${target.platformName.capitalize()}")
      .get()
  val projectTaskName = "xtrasKonanDepsProject${target.platformName.capitalized()}"

  project.tasks.register(projectTaskName) {

    outputs.dir(depsProjectDir)
    doFirst {
      depsProjectDir.asFile.mkdirs()
      depsProjectDir.dir("gradle.properties").asFile.writeText(
        """
        kotlin.native.ignoreDisabledTargets=true
        org.gradle.parallel=false
        org.gradle.unsafe.configuration-cache=false
        
      """.trimIndent()
      )


      depsProjectDir.file("settings.gradle.kts").asFile.also {
        if (!it.exists()) it.createNewFile()
      }

      depsProjectDir.file("build.gradle.kts").asFile.writeText(
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


      depsProjectDir.dir("src/commonMain/kotlin").asFile.apply {
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
    dir = depsProjectDir.asFile
    tasks = listOf("compileKotlin${target.platformName.capitalized()}")
    doFirst {
      project.log("$name: running compileKotlin${target.platformName.capitalized()}")
    }
  }

}

