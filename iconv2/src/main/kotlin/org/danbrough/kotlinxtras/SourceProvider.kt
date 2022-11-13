package org.danbrough.kotlinxtras

import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import java.io.File


interface SourceConfig

typealias SourceProvider = ()->Unit

data class ArchiveSourceConfig(
  var url: String = "",
  var stripTopDir: Boolean = false,
  var tarExtractOptions: String = "xf"
) : SourceConfig

fun BinaryExtension.archiveSource(
  url: String,
  configure: ArchiveSourceConfig.() -> Unit
):SourceProvider = {
  val config = ArchiveSourceConfig(url).also(configure)
  registerSourceDownloadTask(config.url,config.tarExtractOptions,config.stripTopDir)

}


fun BinaryExtension.registerGitDownloadTask(gitUrl: String, commit: String): DownloadSourcesTask =
  project.tasks.register("downloadSources${libName.capitalized()}") {
    val sourcesDir = sourcesDir()
    inputs.property("commit", commit)
    outputs.dir(sourcesDir)
    group = XTRAS_TASK_GROUP

    if (!sourcesDir.resolve(".git").exists()) {
      actions.add {
        project.exec {
          commandLine(buildEnvironment.gitBinary, "init", sourcesDir.absolutePath)
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
      project.exec {
        workingDir = sourcesDir
        commandLine(buildEnvironment.gitBinary, "checkout", "FETCH_HEAD")
      }
    }
  }


fun BinaryExtension.registerDownloadTask(
  taskName: String, url: String, outputFile: File
): TaskProvider<*> = project.tasks.register(taskName) {
  //group = BINARIES_TASK_GROUP
  inputs.property("url", url)

  doFirst {
    val parentDir = outputFile.parentFile
    if (!parentDir.exists()) parentDir.mkdirs()
  }
  outputs.file(outputFile)
  actions.add {
    project.exec {
      commandLine(
        buildEnvironment.wgetBinary, "-q",
        "-c",
        inputs.properties["url"],
        "-P",
        outputFile.parent
      )
      println("running: ${commandLine.joinToString(" ")}")
    }
  }
}

fun BinaryExtension.registerExtractArchiveTask(
  taskName: String, destDir: File, options: String = "xf", stripTopDir: Boolean = false
): TaskProvider<*> = project.tasks.register(taskName) {
  //group = BINARIES_TASK_GROUP
  outputs.dir(destDir)
  doFirst {
    if (!destDir.exists()) destDir.mkdirs()
  }
  actions.add {
    project.exec {
      val archive = inputs.files.first()
      val cmdLine = mutableListOf<String>("tar", options, archive.absolutePath)
      if (stripTopDir) cmdLine += "--strip-components=1"
      cmdLine += listOf("-C" , destDir.absolutePath)
      commandLine(cmdLine)
      println("running: ${commandLine.joinToString(" ")}")    }
  }
}


fun BinaryExtension.registerSourceDownloadTask(url: String,extractOptions:String = "xf",stripTopDir:Boolean = false): DownloadSourcesTask {
  val archiveFile: File = downloadsDir().resolve(url.substringAfterLast('/'))

  val downloadTask =
    registerDownloadTask("downloadArchive${libName.capitalized()}", url, archiveFile)

  val extractTask =
    registerExtractArchiveTask("extractArchive${libName.capitalized()}", sourcesDir(), extractOptions, stripTopDir)

  extractTask.configure {
    inputs.file(archiveFile)
    dependsOn(downloadTask)
  }

  return project.tasks.register("downloadSources${libName.capitalized()}") {
    group = XTRAS_TASK_GROUP
    inputs.property("url", url)
    outputs.dir(sourcesDir())
    dependsOn(extractTask)
  }
}

