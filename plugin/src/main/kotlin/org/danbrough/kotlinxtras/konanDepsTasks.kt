package org.danbrough.kotlinxtras

import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


private val KonanTarget.xtrasKonanDepsTaskName: String
  get() = "xtrasKonanDeps${platformName.capitalized()}"

private val KonanTarget.xtrasKonanDepsProjectPath: File
  get() = File(System.getProperty("java.io.tmpdir"), xtrasKonanDepsTaskName)

fun KonanTarget.registerKonanDepsTask(project: Project) {
  val depsProjectDir = xtrasKonanDepsProjectPath
  val taskName = xtrasKonanDepsTaskName
  val rootProject = project.rootProject

  //if (rootProject.tasks.findByName(taskName) != null) return


  rootProject.tasks.register(taskName) {

    outputs.dir(depsProjectDir)
    doFirst {
      if (depsProjectDir.exists()) depsProjectDir.deleteRecursively()
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
           $platformName()
          }
            
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


  rootProject.tasks.register(
    "xtrasKonanDownload${platformName.capitalized()}", GradleBuild::class.java
  ) {
    dependsOn(xtrasKonanDepsTaskName)
    dir = depsProjectDir
    tasks = listOf("compileKotlin${platformName.capitalized()}")
    doFirst {
      project.log("$name: running compileKotlin${platformName.capitalized()}")
    }
  }
}




