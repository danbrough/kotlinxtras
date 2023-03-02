package org.danbrough.kotlinxtras

import org.gradle.api.Task
import org.gradle.api.tasks.GradleBuild
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


fun Task.enableKonanDeps(target: KonanTarget) {

  val taskName = "xtrasKonanDeps${target.platformName.capitalized()}"
  dependsOn(taskName)

  if (project.tasks.findByName(taskName) != null) return

  val depsProjectDir =
    File(System.getProperty("java.io.tmpdir"), "xtraKonanDeps${target.platformName.capitalize()}")
  val projectTaskName = "xtrasKonanDepsProject${target.platformName.capitalized()}"

  project.tasks.register(projectTaskName) {

    outputs.dir(depsProjectDir)
    doFirst {
      depsProjectDir.mkdirs()
      depsProjectDir.resolve("gradle.properties").writeText(
        """
        kotlin.native.ignoreDisabledTargets=true
      """.trimIndent()
      )

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
    taskName, GradleBuild::class.java
  ) {
    dependsOn(projectTaskName)
    dir = depsProjectDir
    tasks = listOf("compileKotlin${target.platformName.capitalized()}")
    doFirst {
      project.log("$name: running compileKotlin${target.platformName.capitalized()}")
    }
  }

}




