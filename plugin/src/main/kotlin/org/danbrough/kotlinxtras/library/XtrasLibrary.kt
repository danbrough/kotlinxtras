package org.danbrough.kotlinxtras.library

import org.danbrough.kotlinxtras.BuildEnvironment
import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.XtrasDSLMarker
import org.danbrough.kotlinxtras.capitalized
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.source.GitSourceConfig
import org.danbrough.kotlinxtras.source.registerDownloadSourceGit
import org.danbrough.kotlinxtras.tasks.registerArchiveTask
import org.danbrough.kotlinxtras.xtrasBuildDir
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.danbrough.kotlinxtras.xtrasSourceDir
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

@Suppress("LeakingThis", "MemberVisibilityCanBePrivate")
@XtrasDSLMarker
open class XtrasLibrary(val project: Project, val libName: String, val version: String) {
  init {
    project.log("Created XtrasLibrary for ${project.projectDir.absolutePath} name: $libName version:$version")
  }

  interface SourceConfig

  var sourceConfig: SourceConfig? = null

  val buildEnv: BuildEnvironment = BuildEnvironment(this)

  @XtrasDSLMarker
  fun buildEnv(conf: BuildEnvironment.() -> Unit) {
    buildEnv.conf()
  }

  @XtrasDSLMarker
  var supportedTargets: List<KonanTarget> = emptyList()

  fun xtrasTaskName(name: String, target: KonanTarget? = null) =
    "xtras${name.capitalized()}${libName.capitalized()}${target?.platformName?.capitalized() ?: ""}"

  fun downloadSourcesTaskName() = xtrasTaskName("DownloadSources")
  fun extractSourcesTaskName(target: KonanTarget) = xtrasTaskName("ExtractSources", target)
  fun configureTaskName(target: KonanTarget) = xtrasTaskName("configure", target)
  fun buildTaskName(target: KonanTarget) = xtrasTaskName("build", target)

  fun archiveTaskName(target: KonanTarget) = xtrasTaskName("archive", target)

  fun packageFileName(
    konanTarget: KonanTarget,
    name: String = libName,
    packageVersion: String = version
  ): String = packageFileName.invoke(konanTarget, name, packageVersion)

  var packageFileName: (konanTarget: KonanTarget, name: String, packageVersion: String) -> String =
    { konanTarget, name, packageVersion -> "xtras_${name}_${konanTarget.platformName}_${packageVersion}.tar.gz" }

  @XtrasDSLMarker
  var sourcesDir: (KonanTarget) -> File =
    { project.xtrasSourceDir.resolve("$libName/$version/${it.platformName}") }

  @XtrasDSLMarker
  var buildDir: (KonanTarget) -> File =
    { project.xtrasBuildDir.resolve("$libName/$version/${it.platformName}") }

  @XtrasDSLMarker
  var archiveFile: (target: KonanTarget) -> File = { target ->
    project.xtrasPackagesDir.resolve(packageFileName(target))
  }

  override fun toString() = "XtrasLibrary[$libName:$version]"
}

fun XtrasLibrary.xtrasRegisterSourceTask(
  name: String,
  target: KonanTarget,
  configure: Exec.() -> Unit
) =
  project.tasks.register<Exec>(name) {
    workingDir(sourcesDir(target))
    dependsOn(extractSourcesTaskName(target))
    group = XTRAS_TASK_GROUP
    configure()
  }

@XtrasDSLMarker
fun Project.xtrasCreateLibrary(
  libName: String,
  version: String,
  configure: XtrasLibrary.() -> Unit = {}
) = extensions.create(libName, XtrasLibrary::class.java, this, libName, version).apply {
  configure()
  afterEvaluate {
    this@apply.registerTasks()
  }
}


internal fun XtrasLibrary.registerTasks() {
  when (sourceConfig) {
    is GitSourceConfig -> {
      registerDownloadSourceGit()
    }
  }

  supportedTargets.forEach {
    registerArchiveTask(it)
  }
}

