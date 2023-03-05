package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.xtrasDownloadsDir
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


interface SourceConfig

data class ArchiveSourceConfig(
  var stripTopDir: Boolean = false,
  var tarExtractOptions: String = "xf"
) : SourceConfig

data class GitSourceConfig(val commit: String) : SourceConfig


@XtrasDSLMarker
fun LibraryExtension.download(url: String, configure: ArchiveSourceConfig.() -> Unit) {
  sourceURL = url
  sourceConfig = ArchiveSourceConfig().apply {
    tarExtractOptions = when {
      url.endsWith(".tar.gz", true) -> "xfz"
      url.endsWith(".tar.bz2", true) -> "xfj"
      else -> throw Error("Missing tarExtractOptions for $url")
    }
    configure()
  }
}

data class DirectorySourceConfig(var file: File) : SourceConfig

@XtrasDSLMarker
fun LibraryExtension.sourceDir(file: File) {
  sourceConfig = DirectorySourceConfig(file)
}

@XtrasDSLMarker
fun LibraryExtension.git(url: String, commit: String) {
  sourceURL = url
  sourceConfig = GitSourceConfig(commit)
}

internal fun LibraryExtension.registerDirectorySourcesTask(
  srcConfig: DirectorySourceConfig, konanTarget: KonanTarget
) {
  project.tasks.register(extractSourcesTaskName(konanTarget), Copy::class.java) {
    from(srcConfig.file)
    destinationDir = sourcesDir(konanTarget)
  }
}

internal fun LibraryExtension.registerGitDownloadTask(
  srcConfig: GitSourceConfig
) {

  val configRepoTaskName = "${downloadSourcesTaskName}_configRepo"


  project.tasks.register(configRepoTaskName, Exec::class.java) {
    val repoDir = gitRepoDir()
    inputs.property("url", sourceURL)

    outputs.file(repoDir.resolve("config"))
    onlyIf {
      !repoDir.resolve("config").exists()
    }
    /*

    */



    doFirst {
      //repoDir.deleteRecursively()

      project.exec {
        commandLine(binaries.gitBinary, "init", "--bare", repoDir)
        println("running#: ${commandLine.joinToString(" ")}")
      }

      println("running: ${commandLine.joinToString(" ")}")
    }
    outputs.dir(repoDir)
    workingDir = gitRepoDir()
    commandLine(binaries.gitBinary, "remote", "add", "origin", sourceURL)

  }

  project.tasks.register(downloadSourcesTaskName, Exec::class.java) {
    inputs.property("url", sourceURL)
    inputs.property("commit", srcConfig.commit)
    dependsOn(configRepoTaskName)
    val repoDir = gitRepoDir()
    outputs.dir(repoDir)
    group = XTRAS_TASK_GROUP
    workingDir(repoDir)
    commandLine(
      binaries.gitBinary,
      "fetch",
      "--depth",
      "1",
      "origin",
      srcConfig.commit
    )
  }
}


internal fun LibraryExtension.registerGitExtractTask(
  srcConfig: GitSourceConfig,
  konanTarget: KonanTarget
) {

  val extractSourcesTaskName = extractSourcesTaskName(konanTarget)
  val initTaskName = "${extractSourcesTaskName}_init"

  project.tasks.register(initTaskName) {
    mustRunAfter(downloadArchiveTaskName(konanTarget))

    dependsOn(downloadSourcesTaskName)
    val gitRepo = project.tasks.getAt(downloadSourcesTaskName).outputs.files.first()
    val destDir = sourcesDir(konanTarget)
    outputs.dir(destDir)

    doFirst {
      println("Running $name")
    }

    onlyIf {
      !destDir.resolve(".git").exists()
    }

    actions.add {
      project.exec {
        workingDir(destDir)
        commandLine(binaries.gitBinary, "init")
        println("running: ${commandLine.joinToString(" ")}")
      }
    }

    actions.add {
      project.exec {
        workingDir(destDir)
        commandLine(binaries.gitBinary, "remote", "add", "origin", gitRepo)
        println("running: ${commandLine.joinToString(" ")}")
      }
    }
  }

  project.tasks.register(extractSourcesTaskName) {
    group = XTRAS_TASK_GROUP
    dependsOn(initTaskName)

    val destDir = sourcesDir(konanTarget)

    inputs.property("commit", srcConfig.commit)
    outputs.dir(destDir)
    //onlyIf { !isPackageBuilt(konanTarget) }


    actions.add {
      project.exec {
        workingDir = destDir
        commandLine(binaries.gitBinary, "clean", "-xdf")
        println("running: ${commandLine.joinToString(" ")}")
      }
    }

    actions.add {
      project.exec {
        workingDir = destDir
        commandLine(binaries.gitBinary, "fetch", "--depth", "1", "origin", srcConfig.commit)
        println("running: ${commandLine.joinToString(" ")}")
      }
      println("checking out commit:${srcConfig.commit} in $destDir")
    }

    actions.add {
      project.exec {
        workingDir = destDir
        commandLine(binaries.gitBinary, "checkout", srcConfig.commit)
        println("running: ${commandLine.joinToString(" ")}")
      }
    }
  }
}


internal fun LibraryExtension.registerArchiveDownloadTask() {
  project.tasks.register(downloadSourcesTaskName, Exec::class.java) {
    group = XTRAS_TASK_GROUP

    val downloadsDir = project.xtrasDownloadsDir
    val outputFile = downloadsDir.resolve(sourceURL!!.substringAfterLast('/'))
    inputs.property("url", sourceURL!!)
    outputs.file(outputFile)
    enabled = !outputFile.exists()
    doFirst {
      if (!downloadsDir.exists()) downloadsDir.mkdirs()
      println("running: ${commandLine.joinToString(" ")}")
    }
    commandLine(
      binaries.wgetBinary, "-q",
      "-c",
      inputs.properties["url"],
      "-P",
      outputFile.parent
    )
  }
}


internal fun LibraryExtension.registerArchiveExtractTask(
  srcConfig: ArchiveSourceConfig,
  konanTarget: KonanTarget
) {

  project.tasks.register(extractSourcesTaskName(konanTarget), Exec::class.java) {
    group = XTRAS_TASK_GROUP
    mustRunAfter(downloadArchiveTaskName(konanTarget))
    //onlyIf { !isPackageBuilt(konanTarget) }
    dependsOn(downloadSourcesTaskName)
    val destDir = sourcesDir(konanTarget)
    val archiveFile = project.tasks.getAt(downloadSourcesTaskName).outputs.files.first()
    inputs.file(archiveFile)
    outputs.dir(destDir)
    doFirst {
      project.log("extracting $archiveFile to $destDir")
      project.log("running: ${commandLine.joinToString(" ")}")
    }
    workingDir(destDir)
    val cmdLine =
      mutableListOf<String>("tar", srcConfig.tarExtractOptions, archiveFile.absolutePath)
    if (srcConfig.stripTopDir) cmdLine += "--strip-components=1"
    commandLine(cmdLine)
  }
}


