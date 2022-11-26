package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.buildEnvironment
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasDir
import org.danbrough.kotlinxtras.xtrasDownloadsDir
import org.danbrough.kotlinxtras.xtrasLibsDir
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

const val KOTLIN_XTRAS_DIR_NAME = "xtras"
const val XTRAS_TASK_GROUP = "xtras"


@DslMarker
//@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE,AnnotationTarget.FUNCTION)
annotation class BinariesDSLMarker


typealias SourcesTask = Exec.(KonanTarget) -> Unit

@BinariesDSLMarker
fun <T : LibraryExtension> Project.registerLibraryExtension(
  name: String,
  type: Class<T>,
  configure: T.() -> Unit
): LibraryExtension =
  registerLibraryExtension(name, type).apply(configure)

@BinariesDSLMarker
abstract class LibraryExtension(
  val project: Project,
//Unique identifier for a binary package
  var libName: String
) {

  lateinit var binaryConfiguration: BinaryConfigurationExtension

  @BinariesDSLMarker
  open var version: String = "unspecified"

  @BinariesDSLMarker
  open var sourceURL: String? = null

  open fun gitRepoDir(): File = project.xtrasDownloadsDir.resolve("repos/$libName")

  @BinariesDSLMarker
  fun configure(task: SourcesTask) {
    configureTask = task
  }

  @BinariesDSLMarker
  fun build(task: SourcesTask) {
    buildTask = task
  }

  @BinariesDSLMarker
  fun configureTarget(configure: (KonanTarget) -> Unit) {
    configureTargetTask = configure
  }

  @BinariesDSLMarker
  fun install(task: SourcesTask) {
    installTask = task
  }

  internal var cinteropsConfigTask: (CInteropsConfig.() -> Unit)? = null

  @BinariesDSLMarker
  fun cinterops(configure: CInteropsConfig.() -> Unit) {
    cinteropsConfigTask = configure
  }

  internal var sourceConfig: SourceConfig? = null

  internal var configureTask: SourcesTask? = null

  internal var buildTask: SourcesTask? = null

  internal var installTask: SourcesTask? = null

  internal var configureTargetTask: ((KonanTarget) -> Unit)? = null


  val downloadSourcesTaskName: String
    get() = "xtrasDownloadSources${libName.capitalized()}"

  fun extractSourcesTaskName(konanTarget: KonanTarget): String =
    "xtrasExtractSources${libName.capitalized()}${konanTarget.platformName.capitalized()}"

  fun configureSourcesTaskName(konanTarget: KonanTarget): String =
    "xtrasConfigure${libName.capitalized()}${konanTarget.platformName.capitalized()}"

  fun buildSourcesTaskName(konanTarget: KonanTarget, name: String = libName): String =
    "xtrasBuild${name.capitalized()}${konanTarget.platformName.capitalized()}"

  fun provideBinariesTaskName(konanTarget: KonanTarget, name: String = libName): String =
    "xtrasProvide${name.capitalized()}${konanTarget.platformName.capitalized()}"

  fun packageTaskName(konanTarget: KonanTarget, name: String = libName): String =
    "xtrasPackage${name.capitalized()}${konanTarget.platformName.capitalized()}"

  fun generateCInteropsTaskName(name: String = libName): String =
    "xtrasGenerateCInterops${name.capitalized()}"

  fun packageFile(
    konanTarget: KonanTarget,
    name: String = libName,
    packageVersion: String = version
  ): String =
    "xtras_${name}_${konanTarget.platformName}_${packageVersion}.tar.gz"

  open fun sourcesDir(konanTarget: KonanTarget): File =
    project.xtrasDir.resolve("src/$libName/$version/${konanTarget.platformName}")

  open fun prefixDir(
    konanTarget: KonanTarget,
    packageName: String = libName,
    packageVersion: String = version
  ): File =
    project.xtrasLibsDir.resolve("$packageName/$packageVersion/${konanTarget.platformName}")

  val konanTargets: Set<KonanTarget>
    get() = project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.targets?.withType(
      KotlinNativeTarget::class.java
    )?.map { it.konanTarget }?.toSet() ?: emptySet()

  open fun isPackageBuilt(
    konanTarget: KonanTarget,
    name: String = libName,
    packageVersion: String = version
  ): Boolean =
    project.xtrasPackagesDir.resolve(packageFile(konanTarget, name, packageVersion)).exists()


  open fun buildEnvironment(konanTarget: KonanTarget): Map<String, *> =
    konanTarget.buildEnvironment()

}

private fun <T : LibraryExtension> Project.registerLibraryExtension(
  extnName: String,
  type: Class<T>
): T {
  val configuration = extensions.findByType(BinaryConfigurationExtension::class.java) ?: let {
    println("APPLYING BINARY PLUGIN")
    pluginManager.apply(BinaryPlugin::class.java)
    extensions.getByType(BinaryConfigurationExtension::class.java)
  }
  return extensions.create(extnName, type, this)
    .apply {
      binaryConfiguration = configuration
      project.afterEvaluate {
        registerXtrasTasks()
      }
    }
}


private fun LibraryExtension.registerXtrasTasks() {
  val srcConfig = sourceConfig

  println("registerXtrasTasks for $libName")
  when (srcConfig) {
    is ArchiveSourceConfig -> {
      println("doing registerArchiveDownloadTask for $libName")

      registerArchiveDownloadTask(srcConfig)
    }

    is GitSourceConfig -> {
      println("doing registerGitDownloadTask for $libName")
      registerGitDownloadTask(srcConfig)
      println("done registerGitDownloadTask for $libName")
    }
  }

  konanTargets.forEach { konanTarget ->

    configureTargetTask?.invoke(konanTarget)

    when (srcConfig) {
      is ArchiveSourceConfig -> {
        registerArchiveExtractTask(srcConfig, konanTarget)
      }

      is GitSourceConfig -> {
        registerGitExtractTask(srcConfig, konanTarget)
      }
    }

    configureTask?.also {
      registerConfigureSourcesTask(konanTarget)
    }

    buildTask?.also {
      registerBuildSourcesTask(konanTarget)
      registerPackageTask(konanTarget)
    }

    registerProvideBinariesTask(konanTarget)
  }


  registerGenerateInteropsTask()

}




