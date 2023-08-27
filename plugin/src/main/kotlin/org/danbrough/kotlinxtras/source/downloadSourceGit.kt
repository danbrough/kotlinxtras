package org.danbrough.kotlinxtras.source

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.XtrasDSLMarker
import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.xtrasDownloadsDir
import org.gradle.api.tasks.Exec
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


private fun XtrasLibrary.gitExec(vararg args: String, workingDir: File? = null): ExecResult =
  project.exec {
    if (workingDir != null)
      workingDir(workingDir)
    val cmdArgs = args.toMutableList().also { it.add(0, buildEnv.binaries.git) }
    project.log("running ${cmdArgs.joinToString(" ")}")
    commandLine(cmdArgs)
  }

internal fun XtrasLibrary.registerDownloadSourceGit() {
  val config = sourceConfig as GitSourceConfig
  val gitBareRepoDir = project.xtrasDownloadsDir.resolve(libName)
  val library = this

  val downloadSourcesTask = project.tasks.register(downloadSourcesTaskName()) {
    group = XTRAS_TASK_GROUP
    outputs.dir(gitBareRepoDir)
    inputs.property("config", config.hashCode())


    actions.add {
      project.log("deleting $gitBareRepoDir")
      project.delete(gitBareRepoDir)
    }

    actions.add {
      library.gitExec("init", "--bare", gitBareRepoDir.absolutePath).assertNormalExitValue()
    }

    actions.add {
      library.gitExec("remote", "add", "origin", config.url, workingDir = gitBareRepoDir)
    }

    actions.add {
      library.gitExec("fetch", "--depth", "1", "origin", config.commit, workingDir = gitBareRepoDir)
    }

    actions.add {
      library.gitExec("reset", "--soft", "FETCH_HEAD", workingDir = gitBareRepoDir)
    }
  }



  supportedTargets.forEach { target ->
    val sourceDir = sourcesDir(target)

    project.tasks.register<Exec>(extractSourcesTaskName(target)) {
      group = XTRAS_TASK_GROUP
      dependsOn(downloadSourcesTask)
      outputs.dir(sourcesDir(target))
      commandLine(buildEnv.binaries.git, "clone", gitBareRepoDir, sourceDir)
      config.configure.invoke(this)
    }
  }
}
