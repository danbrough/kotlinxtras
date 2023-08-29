package org.danbrough.kotlinxtras.source

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.XtrasDSLMarker
import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.xtrasDownloadsDir
import org.gradle.api.Task
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.process.ExecResult
import java.io.File

data class GitSourceConfig(
  val url: String,
  val commit: String,
  val configure: Exec.() -> Unit = {}
) :
  XtrasLibrary.SourceConfig

@XtrasDSLMarker
fun XtrasLibrary.gitSource(url: String, commit: String, configure: Exec.() -> Unit = {}) {
  sourceConfig = GitSourceConfig(url, commit, configure)
}

private fun XtrasLibrary.gitTask(
  name: String,
  args: List<String> = emptyList(),
  config: Exec.() -> Unit = {}
) =
  project.tasks.register<Exec>(name) {
    environment(buildEnv.getEnvironment())
    commandLine(args.toMutableList().apply {
      add(0, buildEnv.binaries.git)
    })
    doFirst {
      project.log("running: ${commandLine.joinToString(" ")}")
    }
    config()
  }


internal fun XtrasLibrary.registerDownloadSourceGit() {
  val config = sourceConfig as GitSourceConfig
  val repoDir = project.xtrasDownloadsDir.resolve(libName)

  val downloadSourcesTaskName = downloadSourcesTaskName()
  val initTaskName = "${downloadSourcesTaskName}_init"
  val remoteAddTaskName = "${downloadSourcesTaskName}_remote_add"
  val fetchTaskName = "${downloadSourcesTaskName}_fetch"
  val resetTaskName = "${downloadSourcesTaskName}_reset"

  project.tasks.register(downloadSourcesTaskName) {
    group = XTRAS_TASK_GROUP
    outputs.dir(repoDir)
    dependsOn(resetTaskName)
  }

  gitTask(
    initTaskName,
    listOf("init", "--bare", repoDir.absolutePath)
  ) {
    onlyIf {
      !repoDir.exists()
    }
  }

  gitTask(remoteAddTaskName, listOf("remote", "add", "origin", config.url)) {
    dependsOn(initTaskName)
    workingDir(repoDir)
    onlyIf {
      repoDir.resolve("config").let { configFile ->
        configFile.exists() && !configFile.readText().contains(config.url)
      }
    }
  }

  gitTask(fetchTaskName, listOf("fetch", "--depth", "1", "origin", config.commit)) {
    dependsOn(remoteAddTaskName)
    workingDir(repoDir)
    //inputs.property("config", config.hashCode())
    val commitFile = repoDir.resolve("fetch_${config.commit}")
    outputs.file(commitFile)
    onlyIf {
      !commitFile.exists()
    }
    doLast {
      commitFile.writeText(
        repoDir.resolve("FETCH_HEAD").bufferedReader().use {
          val commit = it.readLine().split("\\s+".toRegex(), limit = 2).first()
          project.log("writing $commit to ${commitFile.absolutePath}")
          commit
        }
      )
    }
  }

  gitTask(resetTaskName) {
    inputs.property("config", config.hashCode())
    dependsOn(fetchTaskName)
    doFirst {
      val commit = repoDir.resolve("fetch_${config.commit}").readText()
      commandLine(buildEnv.binaries.git, "reset", "--soft", commit)
    }
    workingDir(repoDir)
    outputs.dir(repoDir)
  }

  supportedTargets.forEach { target ->
    val sourceDir = sourcesDir(target)

    gitTask(
      extractSourcesTaskName(target),
      listOf("clone", repoDir.absolutePath, sourceDir.absolutePath)
    ) {
      group = XTRAS_TASK_GROUP
      doFirst {
        project.delete(sourceDir)
      }
      dependsOn(downloadSourcesTaskName)
      outputs.dir(sourceDir)
      config.configure.invoke(this)
    }
  }
}
