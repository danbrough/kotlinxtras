package org.danbrough.kotlinxtras.tasks

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.platformName
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget

fun XtrasLibrary.registerArchiveTask(target: KonanTarget) =
  project.tasks.register<Exec>(archiveTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description = "Outputs binary archive for $libName:${target.platformName}"
    dependsOn(buildTaskName(target))
    workingDir(buildDir(target))
    val archive = archiveFile(target)
    doFirst {
      project.log("creating archive ${archive.absolutePath}")
    }
    outputs.file(archive)
    commandLine(
      buildEnv.binaries.tar,
      "cvpfz",
      archive.absolutePath,
      "--exclude=**share",
      "--exclude=**pkgconfig",
      "./"
    )
  }
/*
import org.danbrough.kotlinxtras.platformName
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget


fun LibraryExtension.registerCreateArchiveTask(target: KonanTarget): TaskProvider<Task> {
  registerCleanBuildTask(target)

  return project.tasks.register(createArchiveTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description = "Outputs binary archive for $libName:${target.platformName}"
    dependsOn(buildSourcesTaskName(target))
    inputs.dir(buildDir(target))
    val archiveFile = archiveFile(target)
    outputs.file(archiveFile)

    actions.add {
      project.exec {
        workingDir(buildDir(target))
        commandLine(
          "tar",
          "cvpfz",
          archiveFile.absolutePath,
          "--exclude=**share",
          "--exclude=**pkgconfig",
          "./"
        )
      }
    }

    finalizedBy(
      "publish${libName.capitalized()}${target.platformName.capitalized()}PublicationToXtrasRepository",
      //  cleanupTaskName(target)
    )
  }
}
*/
