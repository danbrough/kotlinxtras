package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.platformName
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


internal fun LibraryExtension.registerExtractLibsTask(target: KonanTarget): TaskProvider<Task> =
  project.tasks.register(extractArchiveTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description = "Unpacks $libName:${target.platformName} into the ${libsDir(target)} directory"
    //mustRunAfter(downloadSourcesTaskName(target),buildSourcesTaskName(target))
    mustRunAfter(downloadArchiveTaskName(target), createArchiveTaskName(target))

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

fun LibraryExtension.registerCreateArchiveTask(target: KonanTarget): TaskProvider<Task> =
  project.tasks.register(createArchiveTaskName(target)) {
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
    finalizedBy("publish${libName.capitalized()}${target.platformName.capitalized()}PublicationToXtrasRepository")
  }


fun LibraryExtension.resolveBinariesFromMaven(target: KonanTarget): File? {

  val binariesConfiguration =
    project.configurations.create("configuration${libName.capitalized()}Binaries${target.platformName.capitalized()}") {
      isVisible = false
      isTransitive = false
      isCanBeConsumed = false
      isCanBeResolved = true
    }

  val mavenID = "$publishingGroup:$libName${target.platformName.capitalized()}:$version"
  project.log("LibraryExtension.resolveBinariesFromMaven():$target $mavenID")


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
    val archiveFile = archiveFile(target)
    outputs.file(archiveFile)
    extraProperties["downloaded"] = false
    onlyIf {
      !isPackageBuilt(target)
    }

    actions.add {
      resolveBinariesFromMaven(target)?.also {
        project.log("$name: resolved ${it.absolutePath} copying to $archiveFile")
        it.copyTo(archiveFile, overwrite = true)
        extraProperties["downloaded"] = true
      }
    }
  }


/*
fun LibraryExtension.registerArchiveTasks(target: KonanTarget) {
  project.log("LibraryExtension.registerArchiveTasks: $target group:$publishingGroup version:$version")

  registerCreateArchiveTask(target)
  registerExtractLibsTask(target)


  project.extensions.findByType(PublishingExtension::class.java)?.apply {
    publications.register(
      "$libName${target.platformName.capitalized()}",
      MavenPublication::class.java
    ) {
      artifactId = name
      groupId = this@registerArchiveTasks.publishingGroup
      version = this@registerArchiveTasks.version
      artifact(project.tasks.getByName(resolveArchiveTaskName(target)))
      */
/*project.tasks.getByName("publish${libName.capitalized()}${target.platformName.capitalized()}PublicationToXtrasRepository").apply {
        mustRunAfter(resolveArchiveTaskName(target),createArchiveTaskName(target))
      }*//*

    }
  }

}
*/


