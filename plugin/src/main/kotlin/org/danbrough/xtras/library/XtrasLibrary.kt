package org.danbrough.xtras.library

import org.danbrough.xtras.env.BuildEnvironment
import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasDSLMarker
import org.danbrough.xtras.XtrasPlugin
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.defaultSupportedTargets
import org.danbrough.xtras.env.xtrasBuildEnvironment
import org.danbrough.xtras.log
import org.danbrough.xtras.platformName
import org.danbrough.xtras.source.GitSourceConfig
import org.danbrough.xtras.source.registerDownloadSourceGit
import org.danbrough.xtras.tasks.CInteropsConfig
import org.danbrough.xtras.tasks.konanDepsTaskName
import org.danbrough.xtras.tasks.processStdout
import org.danbrough.xtras.tasks.registerArchiveTasks
import org.danbrough.xtras.tasks.registerGenerateInteropsTask
import org.danbrough.xtras.tasks.registerKonanDepsTasks
import org.danbrough.xtras.xtrasBuildDir
import org.danbrough.xtras.xtrasLibsDir
import org.danbrough.xtras.xtrasLogsDir
import org.danbrough.xtras.xtrasPackagesDir
import org.danbrough.xtras.xtrasSourceDir
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.Writer

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

  @XtrasDSLMarker
  val buildEnvironment: BuildEnvironment = project.xtrasBuildEnvironment()


  var libraryDeps: List<XtrasLibrary> = emptyList()

  /**
   * Whether to attempt to download binary archives from maven.
   */
  @XtrasDSLMarker
  var resolveBinariesFromMaven = true

  @XtrasDSLMarker
  fun buildEnvironment(conf: BuildEnvironment.() -> Unit) {
    buildEnvironment.conf()
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

  fun generateInteropsTaskName() = xtrasTaskName("interops")

  fun archiveFileName(
    konanTarget: KonanTarget,
    name: String = libName,
    packageVersion: String = version
  ): String = packageFileName.invoke(konanTarget, name, packageVersion)

  var packageFileName: (konanTarget: KonanTarget, name: String, packageVersion: String) -> String =
    { konanTarget, name, packageVersion ->
      "${
        publishingGroup.replace(
          '.',
          File.separatorChar
        )
      }${File.separatorChar}xtras_${name}_${konanTarget.platformName}_${packageVersion}.tar.gz"
    }

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

  @XtrasDSLMarker
  var logFile: (taskName: String, target: KonanTarget) -> File = { taskName, target ->
    project.xtrasLogsDir.resolve("$libName/$version/${target.platformName}/$taskName.log")
  }

  internal var cinteropsConfig: (CInteropsConfig.() -> Unit)? = null

  @XtrasDSLMarker
  fun cinterops(configure: CInteropsConfig.() -> Unit) {
    cinteropsConfig = configure
  }

  fun addBuildFlags(target: KonanTarget, env: MutableMap<String, Any>) {
    env["CFLAGS"] = "${env["CFLAGS"] ?: ""} -I${libsDir(target)}/include"
    env["LDFLAGS"] = "${env["LDFLAGS"] ?: ""} -L${libsDir(target)}/lib"
  }

  override fun toString() = "XtrasLibrary[$libName:$version]"

  fun Exec.stdoutToLog(target: KonanTarget): Writer =
    logFile(name, target).let { file ->
      file.parentFile.also {
        if (!it.exists()) it.mkdirs()
      }
      val writer = file.writer()
      processStdout(writer, ::println)
      writer
    }
}

fun XtrasLibrary.xtrasRegisterSourceTask(
  name: String,
  target: KonanTarget,
  configure: Exec.() -> Unit
) =
  project.tasks.register<Exec>(name) {
    workingDir(sourcesDir(target))

    stdoutToLog(target)

    onlyIf {
      !archiveFile(target).exists()
    }

    dependsOn(":${target.konanDepsTaskName}")
    dependsOn(extractSourceTaskName(target))
    environment(buildEnvironment.getEnvironment(target))
    doFirst {

      val outputWriter: (String) -> Unit = {

      }

      project.log("Running ${commandLine.joinToString(" ")} for $libName in $workingDir")

      //project.log("ENV: $environment")
    }
    group = XTRAS_TASK_GROUP


    configure()
  }

@XtrasDSLMarker
fun Project.xtrasCreateLibrary(
  libName: String,
  version: String,
  vararg deps: XtrasLibrary,
  configure: XtrasLibrary.() -> Unit = {}
) = extensions.create<XtrasLibrary>(libName, this, libName, version).apply {
  libraryDeps = deps.toList()
  apply<XtrasPlugin>()

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
    project.registerKonanDepsTasks(it)
    registerArchiveTasks(it)
  }

  registerGenerateInteropsTask()
}

