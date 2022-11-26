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

  /**
   * Konan targets supported by this library.
   *
   * By default all configured targets are supported.
   *
   * Use [konanTargets] to get the list of supported targets.
   */
  @BinariesDSLMarker
  open var supportedTargets: Set<KonanTarget>? = null

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
  val configuration = extensions.findByType(BinaryConfigurationExtension::class.java) ?: let {
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

val LibraryExtension.konanTargets: Set<KonanTarget>
  get() = supportedTargets
    ?: project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.targets?.withType(
      KotlinNativeTarget::class.java
    )?.map { it.konanTarget }?.toSet() ?: emptySet()


private fun LibraryExtension.registerXtrasTasks() {
  val srcConfig = sourceConfig

  when (srcConfig) {
    is ArchiveSourceConfig ->
      registerArchiveDownloadTask(srcConfig)

    is GitSourceConfig ->
      registerGitDownloadTask(srcConfig)
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




