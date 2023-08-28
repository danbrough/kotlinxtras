package org.danbrough.kotlinxtras.tasks

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasLibsDir
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget

fun XtrasLibrary.registerArchiveTasks(target: KonanTarget) {
  registerArchiveTask(target)
  registerExtractArchiveTask(target)
}

private fun XtrasLibrary.registerArchiveTask(target: KonanTarget) =
  project.tasks.register<Exec>(archiveTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description = "Outputs binary archive for $libName:${target.platformName}"
    val archive = archiveFile(target)
    if (!archive.exists())
      dependsOn(buildTaskName(target))
    onlyIf {
      !archive.exists().also {
        if (it) project.log("skipping $name as ${archive.absolutePath} exists.")
      }
    }
    workingDir(buildDir(target))

    doFirst {
      project.log("creating archive ${archive.absolutePath} from $workingDir")
    }
    outputs.file(archive)
    environment(buildEnv.getEnvironment(target))
    commandLine(
      buildEnv.binaries.tar,
      "cvpfz",
      archive.absolutePath,
      "--exclude=**share",
      "--exclude=**pkgconfig",
      "./"
    )
  }


private fun XtrasLibrary.registerExtractArchiveTask(target: KonanTarget) =
  project.tasks.register<Exec>(extractArchiveTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description =
      "Extracts binary archive for $libName:${target.platformName} to ${libsDir(target)}"
    val archive = archiveFile(target)
    val libDir = libsDir(target)

    if (!archive.exists())
      dependsOn(archiveTaskName(target))

    workingDir(libDir)

    doFirst {
      project.log("extracting archive ${archive.absolutePath} to ${libDir.absolutePath}")
      libDir.deleteRecursively()
      project.mkdir(libDir)
    }
    inputs.file(archive)
    outputs.dir(libDir)
    environment(buildEnv.getEnvironment(target))
    commandLine(buildEnv.binaries.tar, "xvpfz", archive.absolutePath)
  }


private fun XtrasLibrary.registerPublishArchiveTask(target: KonanTarget) {

  /*
      if (publishBinaries && (HostManager.hostIsMac == target.family.isAppleFamily)) {
      publishing.publications.create(
        "$libName${target.platformName.capitalize()}", MavenPublication::class.java
      ) {
        artifactId = "${libName}${target.platformName.capitalize()}"
        version = this@registerXtrasTasks.version
        artifact(archiveTask)
        groupId = this@registerXtrasTasks.publishingGroup
      }
    }
   */
}