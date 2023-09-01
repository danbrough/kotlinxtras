package org.danbrough.kotlinxtras.library

import org.danbrough.kotlinxtras.BuildEnvironment
import org.danbrough.kotlinxtras.XTRAS_PACKAGE
import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.XtrasDSLMarker
import org.danbrough.kotlinxtras.capitalized
import org.danbrough.kotlinxtras.defaultSupportedTargets
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.source.GitSourceConfig
import org.danbrough.kotlinxtras.source.registerDownloadSourceGit
import org.danbrough.kotlinxtras.tasks.konanDepsTaskName
import org.danbrough.kotlinxtras.tasks.registerArchiveTasks
import org.danbrough.kotlinxtras.tasks.registerKonanDepsTasks
import org.danbrough.kotlinxtras.xtrasBuildDir
import org.danbrough.kotlinxtras.xtrasLibsDir
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.danbrough.kotlinxtras.xtrasSourceDir
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

@Suppress("LeakingThis", "MemberVisibilityCanBePrivate")
@XtrasDSLMarker
open class XtrasLibrary(val project: Project, val libName: String, val version: String) {
  init {
    project.log("created $this for ${project.projectDir.absolutePath}")
  }

  interface SourceConfig


  @XtrasDSLMarker
  var publishingGroup: String = XTRAS_PACKAGE

  var sourceConfig: SourceConfig? = null

  val buildEnv: BuildEnvironment = BuildEnvironment(this)

  /**
   * Whether to attempt to download binary archives from maven.
   */
  @XtrasDSLMarker
  var resolveBinariesFromMaven = true

  @XtrasDSLMarker
  fun buildEnv(conf: BuildEnvironment.() -> Unit) {
    buildEnv.conf()
  }

  @XtrasDSLMarker
  var supportedTargets: List<KonanTarget> = emptyList()

  fun xtrasTaskName(name: String, target: KonanTarget? = null) =
    "xtras${name.capitalized()}${libName.capitalized()}${target?.platformName?.capitalized() ?: ""}"

  fun downloadSourceTaskName() = xtrasTaskName("DownloadSource")
  fun extractSourceTaskName(target: KonanTarget) = xtrasTaskName("ExtractSource", target)
  fun prepareSourceTaskName(target: KonanTarget) = xtrasTaskName("PrepareSource", target)

  fun configureTaskName(target: KonanTarget) = xtrasTaskName("configure", target)
  fun buildTaskName(target: KonanTarget) = xtrasTaskName("build", target)
  fun archiveTaskName(target: KonanTarget) = xtrasTaskName("archive", target)
  fun extractArchiveTaskName(target: KonanTarget) = xtrasTaskName("extractArchive", target)
  fun provideArchiveTaskName(target: KonanTarget) = xtrasTaskName("provideArchive", target)
  fun provideMavenArchiveTaskName(target: KonanTarget) =
    xtrasTaskName("provideMavenArchive", target)

  fun archiveFileName(
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
  var libsDir: (KonanTarget) -> File =
    { project.xtrasLibsDir.resolve("$libName/$version/${it.platformName}") }

  @XtrasDSLMarker
  var archiveFile: (target: KonanTarget) -> File = { target ->
    project.xtrasPackagesDir.resolve(archiveFileName(target))
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
    dependsOn(target.konanDepsTaskName, extractSourceTaskName(target))
    environment(buildEnv.getEnvironment(target))
    doFirst {
      project.log("ENV: $environment")
    }
    group = XTRAS_TASK_GROUP
    configure()
  }

@XtrasDSLMarker
fun Project.xtrasCreateLibrary(
  libName: String,
  version: String,
  configure: XtrasLibrary.() -> Unit = {}
) = extensions.create<XtrasLibrary>(libName, this, libName, version).apply {
  afterEvaluate {
    if (supportedTargets.isEmpty())
      supportedTargets = defaultSupportedTargets()
    configure()
    this@apply.registerTasks()
  }
}


private fun XtrasLibrary.registerTasks() {


  when (sourceConfig) {
    is GitSourceConfig -> {
      registerDownloadSourceGit()
    }
  }

  supportedTargets.forEach {
    registerArchiveTasks(it)
    project.registerKonanDepsTasks(it)
  }
}

