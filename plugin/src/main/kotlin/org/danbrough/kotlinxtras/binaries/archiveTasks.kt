package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.capitalize
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


private fun LibraryExtension.cleanupTaskName(target: KonanTarget) =
  "xtrasCleanUp${libName.capitalize()}${target.platformName.capitalize()}"

private fun LibraryExtension.registerCleanBuildTask(target: KonanTarget) =
  project.tasks.register(cleanupTaskName(target)) {
    val buildDir = buildDir(target)
    val sourcesDir = sourcesDir(target)
    actions.add {

      if (buildDir.exists()) {
        project.log("${cleanupTaskName(target)} deleting $buildDir")
        buildDir.deleteRecursively()
      }

      if (sourcesDir.exists()) {
        project.log("${cleanupTaskName(target)} deleting $sourcesDir")
        sourcesDir.deleteRecursively()
      }
    }
  }


internal fun LibraryExtension.registerProvideArchiveTask(target: KonanTarget): TaskProvider<Task> =
  project.tasks.register(provideArchiveTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description =
      "Ensures that the binary archive for $libName:${target.platformName} exists in the ${project.xtrasPackagesDir} folder"

    if (deferToPrebuiltPackages)
      dependsOn(downloadArchiveTaskName(target))
    else dependsOn(createArchiveTaskName(target))

    outputs.file(archiveFile(target))
  }


internal fun LibraryExtension.registerExtractLibsTask(target: KonanTarget): TaskProvider<Task> =
  project.tasks.register(extractArchiveTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description = "Unpacks $libName:${target.platformName} into the ${libsDir(target)} directory"
    dependsOn(provideArchiveTaskName(target))

    outputs.dir(libsDir(target))
    actions.add {
      project.exec {
        val archiveFile = archiveFile(target)
        workingDir(libsDir(target))
        project.log("extracting: $archiveFile to $workingDir")
        commandLine("tar", "xvpfz", archiveFile.absolutePath)
      }
    }
  }

fun LibraryExtension.registerCreateArchiveTask(target: KonanTarget): TaskProvider<Task> {
  registerCleanBuildTask(target)

  return project.tasks.register(createArchiveTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description = "Outputs binary archive for $libName:${target.platformName}"
    dependsOn(buildSourcesTaskName(target))
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

    this.destroyables

    finalizedBy(
      "publish${libName.capitalized()}${target.platformName.capitalized()}PublicationToXtrasRepository",
      cleanupTaskName(target)
    )
  }
}

fun LibraryExtension.resolveBinariesFromMaven(target: KonanTarget): File? {


  val mavenID = "$publishingGroup:$libName${target.platformName.capitalized()}:$version"
  project.log("LibraryExtension.resolveBinariesFromMaven():$target $mavenID")

  val binariesConfiguration =
    project.configurations.create("configuration${libName.capitalized()}Binaries${target.platformName.capitalized()}") {
      isVisible = false
      isTransitive = false
      isCanBeConsumed = false
      isCanBeResolved = true
    }

  project.repositories.all {
    if (this is MavenArtifactRepository) {
      project.log("LibraryExtension.resolveBinariesFromMaven():$target REPO: ${this.name}:${this.url}")
    }
  }
  project.dependencies {
    binariesConfiguration(mavenID)
  }

  runCatching {
    return binariesConfiguration.resolve().first().also {
      project.log("LibraryExtension.resolveBinariesFromMaven():$target found ${it.absolutePath}")
    }
  }.exceptionOrNull()?.let {
    project.log("LibraryExtension.resolveBinariesFromMaven():$target Failed for $mavenID: ${it.message}")
  }
  return null
}


internal fun LibraryExtension.registerDownloadArchiveTask(target: KonanTarget): TaskProvider<Task> =
  project.tasks.register(downloadArchiveTaskName(target)) {
    group = XTRAS_TASK_GROUP

    val archiveFile = archiveFile(target)
    outputs.file(archiveFile)


    actions.add {

      resolveBinariesFromMaven(target)?.also {
        project.log("$name: resolved ${it.absolutePath} copying to $archiveFile")
        it.copyTo(archiveFile, overwrite = true)
      }
    }
  }



