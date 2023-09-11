package org.danbrough.xtras.library

import org.danbrough.xtras.PROPERTY_BUILD_DIR
import org.danbrough.xtras.PROPERTY_CINTEROPS_DIR
import org.danbrough.xtras.PROPERTY_DOWNLOADS_DIR
import org.danbrough.xtras.PROPERTY_LIBS_DIR
import org.danbrough.xtras.PROPERTY_PACKAGES_DIR
import org.danbrough.xtras.PROPERTY_SOURCE_DIR
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
import org.gradle.api.Task
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.Writer
import java.util.Locale

@Suppress("LeakingThis", "MemberVisibilityCanBePrivate")
@XtrasDSLMarker
open class XtrasLibrary(val project: Project, val libName: String, val version: String) {
  init {
    project.log("created $this for ${project.projectDir.absolutePath}")
  }

  enum class TaskName(val description: String) {
    DOWNLOAD_SOURCE("Download source repository to $PROPERTY_DOWNLOADS_DIR"),

    EXTRACT_SOURCE("Copy source from $PROPERTY_DOWNLOADS_DIR to $PROPERTY_SOURCE_DIR"),
    PREPARE_SOURCE("Prepare source in $PROPERTY_SOURCE_DIR for the CONFIGURE task"),
    CONFIGURE("Configure source in $PROPERTY_SOURCE_DIR for the BUILD task"),
    BUILD("Build source in $PROPERTY_SOURCE_DIR and install in $PROPERTY_BUILD_DIR"),
    CREATE_ARCHIVE("Create archive from $PROPERTY_BUILD_DIR in $PROPERTY_PACKAGES_DIR"),
    EXTRACT_ARCHIVE("Extract archive from $PROPERTY_PACKAGES_DIR to $PROPERTY_LIBS_DIR"),
    PROVIDE_ARCHIVE("Either build the archive or use PROPERTY_MAVEN_ARCHIVE to download to $PROPERTY_PACKAGES_DIR"),
    PROVIDE_MAVEN_ARCHIVE("Download prebuilt archive from maven to $PROPERTY_PACKAGES_DIR"),
    GENERATE_INTEROPS("Generate interops def file in $PROPERTY_CINTEROPS_DIR"),
    ;

    val taskName: String
      get() = toString()

    override fun toString() =
      name.split("_").joinToString("") { it.lowercase(Locale.getDefault()).capitalized() }
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

  fun xtrasTaskName(name: String, libName: String? = null, target: KonanTarget? = null) =
    "xtras${name.capitalized()}${libName?.capitalized() ?: ""}${target?.platformName?.capitalized() ?: ""}"

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

  fun xtrasTaskName(name: TaskName, target: KonanTarget? = null) =
    "xtras${name.taskName}${libName.capitalized()}${target?.platformName?.capitalized() ?: ""}"

  fun downloadSourceTaskName() = xtrasTaskName(TaskName.DOWNLOAD_SOURCE)
  fun extractSourceTaskName(target: KonanTarget) = xtrasTaskName(TaskName.EXTRACT_SOURCE, target)
  fun prepareSourceTaskName(target: KonanTarget) = xtrasTaskName(TaskName.PREPARE_SOURCE, target)

  fun configureTaskName(target: KonanTarget) = xtrasTaskName(TaskName.CONFIGURE, target)
  fun buildTaskName(target: KonanTarget) = xtrasTaskName(TaskName.BUILD, target)
  fun createArchiveTaskName(target: KonanTarget) = xtrasTaskName(TaskName.CREATE_ARCHIVE, target)
  fun extractArchiveTaskName(target: KonanTarget) = xtrasTaskName(TaskName.EXTRACT_ARCHIVE, target)
  fun provideArchiveTaskName(target: KonanTarget) = xtrasTaskName(TaskName.PROVIDE_ARCHIVE, target)
  fun provideMavenArchiveTaskName(target: KonanTarget) =
    xtrasTaskName(TaskName.PROVIDE_MAVEN_ARCHIVE, target)

  fun generateInteropsTaskName() =
    xtrasTaskName(TaskName.GENERATE_INTEROPS)

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
  task: XtrasLibrary.TaskName,
  target: KonanTarget,
  configure: Exec.() -> Unit
): String {

  val extractSourceTaskName =
    xtrasTaskName(XtrasLibrary.TaskName.EXTRACT_SOURCE.taskName, libName, target)


  return registerXtrasTask<Exec>(task.taskName, target) {
    workingDir(sourcesDir(target))
    description = task.description


    stdoutToLog(target)

    onlyIf {
      !archiveFile(target).exists()
    }

    dependsOn(":${target.konanDepsTaskName}")
    dependsOn(extractSourceTaskName)
    environment(buildEnvironment.getEnvironment(target))
    doFirst {


      project.log("Running ${commandLine.joinToString(" ")} for $libName in $workingDir")

      //project.log("ENV: $environment")
    }
    configure()
  }
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

inline fun <reified T : Task> XtrasLibrary.registerXtrasTask(
  name: String,
  target: KonanTarget? = null,
  noinline configure: T.() -> Unit
): String {

  val taskName = xtrasTaskName(name, libName, target)
  val generalTaskName = xtrasTaskName(name, libName)
  println("registerXtrasTask: name:$name target:$target generalTaskName:$generalTaskName")

  if (project.tasks.findByName(generalTaskName) == null)
    project.tasks.register(generalTaskName) {
      group = XTRAS_TASK_GROUP
      description = "Runs ${generalTaskName}[Target] for all targets"
    }

  println("registerXtrasTask: registering task: $taskName")
  project.tasks.register<T>(taskName) {
    dependsOn(generalTaskName)
    configure()
  }

  return taskName
}

