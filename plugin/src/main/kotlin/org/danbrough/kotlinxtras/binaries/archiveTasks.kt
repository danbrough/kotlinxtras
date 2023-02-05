package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


private fun LibraryExtension.registerExtractLibsTask(target: KonanTarget): TaskProvider<Task> =
  project.tasks.register(extractLibsTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description = "Unpacks $libName:${target.platformName} into the ${libsDir(target)} directory"
    dependsOn(resolveArchiveTaskName(target))
    outputs.dir(libsDir(target))
    actions.add {
      project.exec {
        val archiveFile =
          project.tasks.getByName(resolveArchiveTaskName(target)).outputs.files.first()

        workingDir(libsDir(target))
        project.log("extracting: $archiveFile to $workingDir")
        commandLine("tar", "xvpfz", archiveFile.absolutePath)
      }
    }
  }

private fun LibraryExtension.registerCreateArchiveTask(target: KonanTarget): TaskProvider<Task> =
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
          "--exclude=share",
          "--exclude=libs/pkgconfig*",
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

private fun LibraryExtension.registerResolveArchiveTask(target: KonanTarget): TaskProvider<Task> =
  project.tasks.register(resolveArchiveTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description = "Resolves binary archive for $libName:${target.platformName}"

    finalizedBy(extractLibsTaskName(target))

    val archiveFile = if (enablePrebuiltPackages) resolveBinariesFromMaven(target)?.also {
      project.log("$name: resolved ${it.absolutePath}")
      outputs.file(it)
    } else null

    if (archiveFile == null){
      project.log("$name: $target not available.")

      val createArchiveTask = project.tasks.getByName(createArchiveTaskName(target))
      //need to build the package
      dependsOn(createArchiveTask)
      outputs.file(createArchiveTask.outputs.files.first())
    }
  }

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
      artifact(registerResolveArchiveTask(target))
      project.tasks.getByName("publish${libName.capitalized()}${target.platformName.capitalized()}PublicationToXtrasRepository").apply {
        mustRunAfter(resolveArchiveTaskName(target),createArchiveTaskName(target))
      }
    }
  }

}


