package org.danbrough.kotlinxtras

import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


typealias XtrasTask = BinaryExtension.(KonanTarget?) -> TaskProvider<*>

interface SourceConfig

data class ArchiveSourceConfig(
  var stripTopDir: Boolean = false,
  var tarExtractOptions: String = "xf"
) : SourceConfig

data class GitSourceConfig(val commit: String) : SourceConfig

@BinariesDSLMarker
fun BinaryExtension.archiveConfig(configure: ArchiveSourceConfig.() -> Unit) {
  sourceConfig = sourceConfig?.also { throw Error("Source already configured") }
    ?: ArchiveSourceConfig().also(configure)
}

@BinariesDSLMarker
fun BinaryExtension.gitConfig(commit: String) {
  sourceConfig = sourceConfig?.also { throw Error("Source already configured") }
    ?: GitSourceConfig(commit)
}

/*@BinariesDSLMarker
fun BinaryExtension.gitSource(url: String, commit: String) {
  downloadSourcesTask?.run { throw Error("sourcesTask already set") }
  downloadSourcesTask = {
    registerGitDownloadTask(url, commit)
  }
}*/

/*
  project.tasks.register(downloadSourcesTaskName) {
    inputs.property("url", gitUrl)
    inputs.property("commit", commit)
    outputs.dir(sourcesDir)
    group = XTRAS_TASK_GROUP

    if (!sourcesDir.resolve("config").exists()) {
      actions.add {
        project.exec {
          commandLine(buildEnvironment.gitBinary, "init", "--bare", downloadsDir.absolutePath)
        }
        project.exec {
          workingDir = sourcesDir
          commandLine(buildEnvironment.gitBinary, "remote", "add", "origin", gitUrl)
        }
      }
    }

    actions.add {
      project.exec {
        workingDir = sourcesDir
        commandLine(buildEnvironment.gitBinary, "fetch", "--depth", "1", "origin", commit)
      }
//      project.exec {
//        workingDir = sourcesDir
//        commandLine(buildEnvironment.gitBinary, "checkout", "FETCH_HEAD")
//      }
    }
  }
 */

internal fun BinaryExtension.registerGitDownloadTask(
  srcConfig: GitSourceConfig
) {

  val configTaskName = "${downloadSourcesTaskName}_configRepo"
  project.tasks.register(configTaskName) {
    val repoDir = gitRepoDir()
    inputs.property("url", sourceURL)
    inputs.property("commit", srcConfig.commit)
    outputs.dir(repoDir)
    doFirst {
      project.exec {
        commandLine(buildEnvironment.gitBinary, "init", "--bare", repoDir)
        println("running: ${commandLine.joinToString(" ")}")
      }
    }
    actions.add {
      project.exec {
        workingDir = gitRepoDir()
        commandLine(buildEnvironment.gitBinary, "remote", "add", "origin", sourceURL)
        println("running: ${commandLine.joinToString(" ")}")
      }
    }
  }

  project.tasks.register(downloadSourcesTaskName, Exec::class.java) {
    inputs.property("url", sourceURL)
    inputs.property("commit", srcConfig.commit)
    dependsOn(configTaskName)
    val repoDir = gitRepoDir()
    outputs.dir(repoDir)
    group = XTRAS_TASK_GROUP
    workingDir(repoDir)
    commandLine(
      buildEnvironment.gitBinary,
      "fetch",
      "--depth",
      "1",
      "origin",
      srcConfig.commit
    )
  }
}


private fun BinaryExtension.registerGitExtractTask(
  gitUrl: String,
  commit: String
): DownloadSourcesTask =
  project.tasks.register(downloadSourcesTaskName) {
    inputs.property("url", gitUrl)
    inputs.property("commit", commit)
    val sourcesDir = project.file("build/dddd")
    outputs.dir(sourcesDir)
    group = XTRAS_TASK_GROUP

    if (!sourcesDir.resolve("config").exists()) {
      actions.add {
        project.exec {
          commandLine(buildEnvironment.gitBinary, "init", "--bare", sourcesDir.absolutePath)
        }
        project.exec {
          workingDir = sourcesDir
          commandLine(buildEnvironment.gitBinary, "remote", "add", "origin", gitUrl)
        }
      }
    }

    actions.add {
      project.exec {
        workingDir = sourcesDir
        commandLine(buildEnvironment.gitBinary, "fetch", "--depth", "1", "origin", commit)
      }
//      project.exec {
//        workingDir = sourcesDir
//        commandLine(buildEnvironment.gitBinary, "checkout", "FETCH_HEAD")
//      }
    }
  }


internal fun BinaryExtension.registerArchiveDownloadTask(srcConfig:ArchiveSourceConfig){
  project.tasks.register(downloadSourcesTaskName,Exec::class.java) {
    group = XTRAS_TASK_GROUP

    val outputFile = downloadsDir.resolve(sourceURL!!.substringAfterLast('/'))
    inputs.property("url", sourceURL!!)
    outputs.file(outputFile)
    doFirst {
      if (!downloadsDir.exists()) downloadsDir.mkdirs()
      println("running: ${commandLine.joinToString(" ")}")
    }
    commandLine(
      buildEnvironment.wgetBinary, "-q",
      "-c",
      inputs.properties["url"],
      "-P",
      outputFile.parent
    )
  }
}


internal fun BinaryExtension.registerArchiveExtractTask(srcConfig: ArchiveSourceConfig,konanTarget:KonanTarget){
  project.tasks.register(extractSourcesTaskName(konanTarget),Exec::class.java){
    group = XTRAS_TASK_GROUP
    dependsOn(downloadSourcesTaskName)
    val destDir = sourcesDir(konanTarget)
    val archiveFile = project.tasks.getAt(downloadSourcesTaskName).outputs.files.first()
    inputs.file(archiveFile)
    outputs.dir(destDir)
    doFirst {
     // if (!destDir.exists()) destDir.mkdirs()
      println("extracting $archiveFile to $destDir")
      println("running: ${commandLine.joinToString(" ")}")
    }
    workingDir(destDir)
    val cmdLine = mutableListOf<String>("tar", srcConfig.tarExtractOptions, archiveFile.absolutePath)
    if (srcConfig.stripTopDir) cmdLine += "--strip-components=1"
    //cmdLine += listOf("-C", destDir.absolutePath)
    commandLine(cmdLine)
  }
}


@Deprecated("Not needed")
private fun BinaryExtension.registerExtractArchiveTask(
  taskName: String, destDir: File, options: String = "xf", stripTopDir: Boolean = false
): TaskProvider<*> = project.tasks.register(taskName) {
  outputs.dir(destDir)
  doFirst {
    if (!destDir.exists()) destDir.mkdirs()
  }
  actions.add {
    project.exec {
      val archive = inputs.files.first()
      val cmdLine = mutableListOf<String>("tar", options, archive.absolutePath)
      if (stripTopDir) cmdLine += "--strip-components=1"
      cmdLine += listOf("-C", destDir.absolutePath)
      commandLine(cmdLine)
      println("running: ${commandLine.joinToString(" ")}")
    }
  }
}


