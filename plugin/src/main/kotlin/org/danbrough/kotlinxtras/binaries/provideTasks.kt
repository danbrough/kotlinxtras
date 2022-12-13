package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasLibsDir
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.konan.target.KonanTarget

fun LibraryExtension.registerProvideBinariesTask(target: KonanTarget): TaskProvider<Task> {

  val resolveTaskName = "${provideBinariesTaskName(target)}_resolve"

  project.tasks.register(resolveTaskName) {

    val binariesConfiguration =
      project.configurations.create("configuration${libName.capitalized()}Binaries${target.platformName.capitalized()}") {
        isVisible = false
        isTransitive = false
        isCanBeConsumed = false
        isCanBeResolved = true
      }

    project.dependencies {
      binariesConfiguration("$publishingGroup:$libName${target.platformName.capitalized()}:$version")
    }


//    val archives = binariesConfiguration.resolve()
//    if (archives.size != 1) throw Error("Expecting one file in $archives")
//    val archive = archives.first()
    outputs.file(binariesConfiguration)

    doFirst {
      println("running ${this@register.name}")
    }
  }

  return project.tasks.register(provideBinariesTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description = "Provide all binaries for the $libName LibraryExtension"

    /*    if (buildEnabled && target.family.isAppleFamily == HostManager.hostIsMac) {
      if (buildTask == null) throw Error("buildTask not configured for $libName")
      dependsOn(packageTaskName(target))
      return@register
    }*/

    //Download the prebuilt binaries from maven
    println("Configuring $name")

    doFirst {
      println("Running $name")
    }


    //val outputDir = project.xtrasLibsDir.resolve("$libName/$version/${target.platformName}")
    val outputDir = libsDir(target)

    outputs.dir(outputDir)

    val packageFile = project.xtrasPackagesDir.resolve(packageFile(target))
    if (packageFile.exists()) {
      println("found packageFile: $packageFile")
      outputs.file(packageFile)
      actions.add {
        project.logger.info("extracting ${packageFile.absolutePath}")
        println("extracting package ${packageFile.absolutePath}")

        project.exec {
          workingDir(outputDir)
          commandLine(binaries.tarBinary, "xfz", packageFile.absolutePath)
        }
      }
    } else {
      println("adding depends on $resolveTaskName")
      dependsOn(resolveTaskName)
      actions.add {

        val archive = project.tasks.getByName(resolveTaskName).outputs.files.first()
        println("running action to extract: ${ project.tasks.getByName(resolveTaskName).outputs.files.files}")
        project.logger.info("extracting archive ${archive.absolutePath}")
        project.exec {
          workingDir(outputDir)
          commandLine(binaries.tarBinary, "xfz", archive.absolutePath)
        }
      }
    }


  }
}
