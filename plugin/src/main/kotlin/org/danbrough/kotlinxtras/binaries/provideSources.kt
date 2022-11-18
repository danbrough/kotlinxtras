package org.danbrough.kotlinxtras.binaries

import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget


interface SourceConfig

data class ArchiveSourceConfig(
  var stripTopDir: Boolean = false,
  var tarExtractOptions: String = "xf"
) : SourceConfig

data class GitSourceConfig(val commit: String) : SourceConfig

@BinariesDSLMarker
fun BinaryExtension.downloadSources(url: String, configure: ArchiveSourceConfig.() -> Unit) {
  sourceConfig?.also { throw Error("Source already configured") }
  sourceURL = url
  sourceConfig = ArchiveSourceConfig().also(configure)
}

@BinariesDSLMarker
fun BinaryExtension.downloadGitSources(url: String, commit: String) {
  sourceConfig?.also { throw Error("Source already configured") }
  sourceURL = url
  sourceConfig = GitSourceConfig(commit)
}


internal fun BinaryExtension.registerGitDownloadTask(
  srcConfig: GitSourceConfig
) {

  val configRepoTaskName = "${downloadSourcesTaskName}_configRepo"

  project.tasks.register(configRepoTaskName, Exec::class.java) {
    val repoDir = gitRepoDir()
    inputs.property("url", sourceURL)

    //outputs.file(repoDir.resolve("config"))
    onlyIf {
      !repoDir.resolve("config").exists()
    }

    doFirst {
      project.exec {
        commandLine(buildEnvironment.gitBinary, "init", "--bare", repoDir)
        println("running#: ${commandLine.joinToString(" ")}")
      }


      println("running-: ${commandLine.joinToString(" ")}")
    }
    workingDir = gitRepoDir()
    commandLine(buildEnvironment.gitBinary, "remote", "add", "origin", sourceURL)
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
      buildEnvironment.gitBinary,
      "fetch",
      "--depth",
      "1",
      "origin",
      srcConfig.commit
    )
  }
}


internal fun BinaryExtension.registerGitExtractTask(
  srcConfig: GitSourceConfig,
  konanTarget: KonanTarget
) {

  val extractSourcesTaskName = extractSourcesTaskName(konanTarget)
  val initTaskName = "${extractSourcesTaskName}_init"

  project.tasks.register(initTaskName) {
    dependsOn(downloadSourcesTaskName)
    val gitRepo = project.tasks.getAt(downloadSourcesTaskName).outputs.files.first()
    val destDir = sourcesDir(konanTarget)
    outputs.dir(destDir)
    onlyIf {
      !destDir.resolve(".git").exists()
    }

    actions.add {
      project.exec {
        workingDir = destDir
        commandLine(buildEnvironment.gitBinary, "init")
        println("running: ${commandLine.joinToString(" ")}")
      }
    }

    actions.add {
      project.exec {
        workingDir = destDir
        commandLine(buildEnvironment.gitBinary, "remote", "add", "origin", gitRepo)
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

    actions.add {
      project.exec {
        workingDir = destDir
        commandLine(buildEnvironment.gitBinary, "fetch", "--depth", "1", "origin", srcConfig.commit)
        println("running: ${commandLine.joinToString(" ")}")
      }
      println("checking out commit:${srcConfig.commit} in $destDir")
    }

    actions.add {
      project.exec {
        workingDir = destDir
        commandLine(buildEnvironment.gitBinary, "checkout", srcConfig.commit)
        println("running: ${commandLine.joinToString(" ")}")
      }
    }
  }
}


internal fun BinaryExtension.registerArchiveDownloadTask(srcConfig: ArchiveSourceConfig) {
  project.tasks.register(downloadSourcesTaskName, Exec::class.java) {
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


internal fun BinaryExtension.registerArchiveExtractTask(
  srcConfig: ArchiveSourceConfig,
  konanTarget: KonanTarget
) {

  project.tasks.register(extractSourcesTaskName(konanTarget), Exec::class.java) {
    group = XTRAS_TASK_GROUP
    dependsOn(downloadSourcesTaskName)
    val destDir = sourcesDir(konanTarget)
    val archiveFile = project.tasks.getAt(downloadSourcesTaskName).outputs.files.first()
    inputs.file(archiveFile)
    outputs.dir(destDir)
    doFirst {
      println("extracting $archiveFile to $destDir")
      println("running: ${commandLine.joinToString(" ")}")
    }
    workingDir(destDir)
    val cmdLine = mutableListOf<String>("tar", srcConfig.tarExtractOptions, archiveFile.absolutePath)
    if (srcConfig.stripTopDir) cmdLine += "--strip-components=1"
    commandLine(cmdLine)
  }
}


