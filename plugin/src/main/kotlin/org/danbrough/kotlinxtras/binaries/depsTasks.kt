package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasSupportedTargets
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


private const val XTRAS_DEPS_PROJECT_TASK_NAME = "xtrasKonanDepsProject"


val KonanTarget.xtrasKonanDepsTaskName: String
  get() = "xtrasKonanDeps${platformName.capitalized()}"


fun Project.registerDepsTask() {
  val depsProjectDir = File(System.getProperty("java.io.tmpdir"), XTRAS_DEPS_PROJECT_TASK_NAME)

  tasks.findByName(XTRAS_DEPS_PROJECT_TASK_NAME) ?: run {
    tasks.register(XTRAS_DEPS_PROJECT_TASK_NAME) {

      outputs.dir(depsProjectDir)
      doFirst {
        if (depsProjectDir.exists()) depsProjectDir.deleteRecursively()
        depsProjectDir.mkdirs()
        depsProjectDir.resolve("gradle.properties").writeText(
          """
        kotlin.native.ignoreDisabledTargets=true
      """.trimIndent()
        )

        depsProjectDir.resolve("build.gradle.kts").writeText("""
          plugins {
            kotlin("multiplatform") version "${KotlinVersion.CURRENT}"
          }
          
          kotlin {
            ${
          xtrasSupportedTargets.joinToString("\n") {
            "${it.platformName}()"
          }
        }
            
          }
          
          repositories {
            mavenCentral()
          }

      """.trimIndent())


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


    xtrasSupportedTargets.forEach {
      tasks.register("xtrasKonanDeps${it.platformName.capitalized()}", GradleBuild::class.java) {
        dependsOn(XTRAS_DEPS_PROJECT_TASK_NAME)
        dir = depsProjectDir
        tasks = listOf("compileKotlin${it.platformName.capitalized()}")
        doFirst {
          println("RUNNING compileKotlin${it.platformName.capitalized()}")
        }
      }
    }
  }


}



