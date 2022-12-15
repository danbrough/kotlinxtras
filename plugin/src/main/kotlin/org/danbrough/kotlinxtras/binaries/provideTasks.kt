package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


fun LibraryExtension.resolveBinariesFromMaven(target: KonanTarget): File? {
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

  return runCatching {
    binariesConfiguration.resolve().first()
  }.exceptionOrNull()?.let {
    null
  }
}

fun LibraryExtension.registerProvideBinariesTask(target: KonanTarget): TaskProvider<Task> =
  project.tasks.register(provideBinariesTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description = "Provide $target binaries for the $libName LibraryExtension"

    val libsDir = libsDir(target)

    outputs.dir(libsDir)

    var packageFile = project.xtrasPackagesDir.resolve(packageFileName(target))

    if (!packageFile.exists()) {
      resolveBinariesFromMaven(target)?.also {
        println("$name: found binaries from maven")
        packageFile = it
      } ?: dependsOn(packageTaskName(target)).also {
        println("$name: adding depends on packageTask for $target")
      }
    }

    doLast {
      if (!packageFile.exists()) {
        println("$name: trying again to get binaries from maven..")
        packageFile =
          resolveBinariesFromMaven(target) ?: throw Error("Failed to provide $name for $target")
      }
      println("$name: extracting archive ${packageFile.absolutePath}")
      project.exec {
        workingDir(libsDir)
        commandLine(binaries.tarBinary, "xfz", packageFile.absolutePath)
      }
    }
  }
