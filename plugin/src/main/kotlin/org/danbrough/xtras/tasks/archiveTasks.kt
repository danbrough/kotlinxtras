package org.danbrough.xtras.tasks

import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.library.XtrasLibrary
import org.danbrough.xtras.log
import org.danbrough.xtras.platformName
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

fun XtrasLibrary.registerArchiveTasks(target: KonanTarget) {
  registerArchiveTask(target)
  registerProvideArchiveTask(target)
  registerExtractArchiveTask(target)
  registerPublishArchiveTask(target)
  registerMavenArchiveTask(target)
}

private fun XtrasLibrary.registerArchiveTask(target: KonanTarget) =
  project.tasks.register<Exec>(createArchiveTaskName(target)) {
    group = org.danbrough.xtras.XTRAS_TASK_GROUP
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
    shouldRunAfter(provideMavenArchiveTaskName(target))

    doFirst {
      project.log("creating archive ${archive.absolutePath} from $workingDir")
    }
    outputs.file(archive)
    environment(buildEnvironment.getEnvironment(target))
    commandLine(
      buildEnvironment.binaries.tar,
      "cvpfz",
      archive.absolutePath,
      "--exclude=**share",
      "--exclude=**pkgconfig",
      "./"
    )
  }

/*
private fun XtrasLibrary.registerArchiveTask(target: KonanTarget) =
  project.tasks.register<Exec>(createArchiveTaskName(target)) {
    group = org.danbrough.xtras.XTRAS_TASK_GROUP
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
    shouldRunAfter(provideMavencreateArchiveTaskName(target))

    doFirst {
      project.log("creating archive ${archive.absolutePath} from $workingDir")
    }
    outputs.file(archive)
    environment(buildEnvironment.getEnvironment(target))
    commandLine(
      buildEnvironment.binaries.tar,
      "cvpfz",
      archive.absolutePath,
      "--exclude=**share",
      "--exclude=**pkgconfig",
      "./"
    )
  }

 */



const val extractAllTaskName = "xtrasExtractAll"

private fun XtrasLibrary.extractAllTask() = project.tasks.findByName(extractAllTaskName) ?:
project.tasks.create(extractAllTaskName){
  group = org.danbrough.xtras.XTRAS_TASK_GROUP
  description = "Extracts all binary archives"
}

private fun XtrasLibrary.registerExtractArchiveTask(target: KonanTarget) {
  val extractAllTask = extractAllTask()
  val taskName = extractArchiveTaskName(target)
  extractAllTask.dependsOn(taskName)

  project.tasks.register<Exec>(taskName) {
    group = XTRAS_TASK_GROUP
    description =
      "Extracts binary archive for $libName:${target.platformName} to ${libsDir(target)}"
    val archive = archiveFile(target)
    val libDir = libsDir(target)

    if (!archive.exists())
      dependsOn(createArchiveTaskName(target))

    onlyIf {
      archive.exists()
    }

    workingDir(libDir)

    doFirst {
      project.log("extracting archive ${archive.absolutePath} to ${libDir.absolutePath}")
      libDir.deleteRecursively()
      project.mkdir(libDir)
    }
    inputs.file(archive)
    outputs.dir(libDir)
    environment(buildEnvironment.getEnvironment(target))
    commandLine(buildEnvironment.binaries.tar, "xvpfz", archive.absolutePath)
  }
}

fun XtrasLibrary.resolveBinariesFromMaven(target: KonanTarget): File? {
  val mavenID = "$publishingGroup:$libName${target.platformName.capitalized()}:$version"
  project.log("XtrasLibrary.resolveBinariesFromMaven():$target $mavenID")

  val binariesConfiguration =
    project.configurations.create("configuration${libName.capitalized()}Binaries${target.platformName.capitalized()}") {
      isVisible = false
      isTransitive = false
      isCanBeConsumed = false
      isCanBeResolved = true
    }

  project.repositories.all {
    if (this is MavenArtifactRepository) {
      project.log("XtrasLibrary.resolveBinariesFromMaven():$target REPO: ${this.name}:${this.url}")
    }
  }
  project.dependencies {
    binariesConfiguration(mavenID)
  }

  runCatching {
    return binariesConfiguration.resolve().first().also {
      project.log("XtrasLibrary.resolveBinariesFromMaven():$target found ${it.absolutePath}")
    }
  }.exceptionOrNull()?.let {
    project.log("XtrasLibrary.resolveBinariesFromMaven():$target Failed for $mavenID: ${it.message}")
  }
  return null
}


private fun XtrasLibrary.registerMavenArchiveTask(target: KonanTarget) =
  project.tasks.register<Task>(provideMavenArchiveTaskName(target)) {
    val archive = archiveFile(target)
    group = org.danbrough.xtras.XTRAS_TASK_GROUP
    description = "Downloads binary archive for $this to $archive"
    val archiveFile = archiveFile(target)
    outputs.file(archiveFile)
    onlyIf {
      !archiveFile.exists()
    }
    extraProperties["downloaded"] = false

    doFirst {
      resolveBinariesFromMaven(target)?.also {

        it.copyTo(archiveFile, overwrite = true)
        extraProperties["downloaded"] = true
        project.log("copied ${it.absolutePath} to ${archiveFile.absolutePath}")
      }
    }

    doLast {
      project.log("downloaded: ${extraProperties["downloaded"]}")
    }
  }


private fun XtrasLibrary.registerProvideArchiveTask(target: KonanTarget) {
  project.tasks.register<Task>(provideArchiveTaskName(target)) {
    val archive = archiveFile(target)
    group = XTRAS_TASK_GROUP
    description = "Builds or downloads binary archive for $this to $archive"

    onlyIf {
      !archive.exists()
    }

    outputs.file(archive)
    if (resolveBinariesFromMaven)
      dependsOn(provideMavenArchiveTaskName(target))
    dependsOn(createArchiveTaskName(target))

  }
}

private fun XtrasLibrary.registerPublishArchiveTask(target: KonanTarget) {
  project.apply<MavenPublishPlugin>()
  project.extensions.findByType<PublishingExtension>()!!.apply {
    publications.create<MavenPublication>("$libName${target.platformName.capitalized()}") {
      artifactId = "$libName${target.platformName.capitalized()}"
      version = this@registerPublishArchiveTask.version
      groupId = publishingGroup
      val archiveTask = project.tasks.getByName(createArchiveTaskName(target))
      artifact(archiveTask.outputs.files.first()).builtBy(archiveTask)
    }
  }
}