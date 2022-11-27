package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.*
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

@DslMarker
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

  @BinariesDSLMarker
  open var publishingGroup: String = "org.danbrough.kotlinxtras.binaries"

  @BinariesDSLMarker
  open var buildEnabled: Boolean = false

  /**
   * Konan targets supported by this library.
   *
   * By default all configured targets are supported.
   *
   * Use [konanTargets] to get the list of supported targets.
   */
  @BinariesDSLMarker
  open var supportedTargets: List<KonanTarget>? = null

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

  fun provideAllBinariesTaskName(name: String = libName): String =
    "xtrasProvide${name.capitalized()}"

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
  val configuration = extensions.findByName(XTRAS_BINARIES_EXTN_NAME) ?: let {
    logger.info("applying BinaryPlugin to $name")
    pluginManager.apply(BinaryPlugin::class.java)
    extensions.getByName(XTRAS_BINARIES_EXTN_NAME)
  }

  return extensions.create(extnName, type, this)
    .apply {
      binaryConfiguration = configuration as BinaryConfigurationExtension
      project.afterEvaluate {
        registerXtrasTasks()
      }
    }
}

/**
 * Returns the [LibraryExtension.supportedTargets] if configured else returns all the configured
 * kotlin multi-platform targets.
 * If this isn't a kotlin multiplatform project then it returns
 *
 */
val LibraryExtension.konanTargets: List<KonanTarget>
  get() = supportedTargets
    ?: project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.targets?.withType(
      KotlinNativeTarget::class.java
    )?.map { it.konanTarget } ?: project.xtrasSupportedTargets


private fun LibraryExtension.registerXtrasTasks() {
  val srcConfig = sourceConfig

  project.logger.info("registerXtrasTasks for $libName")

  if (buildTask != null && buildEnabled) {
    when (srcConfig) {
      is ArchiveSourceConfig -> {
        registerArchiveDownloadTask()
      }

      is GitSourceConfig -> {
        registerGitDownloadTask(srcConfig)
      }
    }
  }


  konanTargets.forEach { konanTarget ->

    configureTargetTask?.invoke(konanTarget)

    if (buildTask != null && buildEnabled) {

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
      registerBuildSourcesTask(konanTarget)
      registerPublishingTask(konanTarget)
    } else {
      project.logger.info("buildSupport disabled for $libName as either buildTask is null or buildingEnabled is false")
    }

    registerProvideBinariesTask(konanTarget)
  }

  registerGenerateInteropsTask()

}




