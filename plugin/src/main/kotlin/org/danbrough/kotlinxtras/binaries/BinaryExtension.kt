package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.buildEnvironment
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasDir
import org.danbrough.kotlinxtras.xtrasDownloadsDir
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.Serializable

const val KOTLIN_XTRAS_DIR_NAME = "xtras"
const val XTRAS_TASK_GROUP = "xtras"


@DslMarker
//@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE,AnnotationTarget.FUNCTION)
annotation class BinariesDSLMarker

interface Configuration : Serializable {
  var gitBinary: String
  var wgetBinary: String
  var tarBinary: String
}

data class DefaultConfiguration(
  override var gitBinary: String = "/usr/bin/git",
  override var wgetBinary: String = "/usr/bin/wget",
  override var tarBinary: String = "/usr/bin/tar"
) : Configuration


typealias SourcesTask = Exec.(KonanTarget) -> Unit

@BinariesDSLMarker
fun Project.registerBinariesExtension(
  name: String,
  configuration: Configuration = DefaultConfiguration(),
  configure: BinaryExtension.() -> Unit
): BinaryExtension =
  registerBinariesExtension(name, configuration).apply {
    configure()
  }

@BinariesDSLMarker
open class BinaryExtension(
  val project: Project,
//Unique identifier for a binary package
  var libName: String,
  var configuration: Configuration = DefaultConfiguration()
) {

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

  fun packageTaskName(konanTarget: KonanTarget, name: String = libName): String =
    "xtrasPackage${name.capitalized()}${konanTarget.platformName.capitalized()}"
  fun interopsTaskName(konanTarget: KonanTarget, name: String = libName): String =
    "xtrasInterops${name.capitalized()}${konanTarget.platformName.capitalized()}"

  fun packageFile(
    konanTarget: KonanTarget,
    name: String = libName,
    packageVersion: String = version
  ): String =
    "xtras_${name}_${konanTarget.platformName}_${packageVersion}.tar.gz"

  open fun sourcesDir(konanTarget: KonanTarget): File =
    project.xtrasDir.resolve("src/$libName/$version/${konanTarget.platformName}")

  open fun prefixDir(konanTarget: KonanTarget): File =
    project.xtrasDir.resolve("$libName/$version/${konanTarget.platformName}")

  val konanTargets: Set<KonanTarget>
    get() = project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.targets?.withType(
      KotlinNativeTarget::class.java
    )?.map { it.konanTarget }?.toSet() ?: emptySet()

  open fun isPackageBuilt(
    konanTarget: KonanTarget,
    name: String = libName,
    packageVersion: String = version
  ): Boolean = project.xtrasPackagesDir.resolve(packageFile(konanTarget, name, packageVersion)).exists()


  open fun buildEnvironment(konanTarget: KonanTarget): Map<String, *> =
    konanTarget.buildEnvironment()


}

internal fun Project.registerBinariesExtension(
  extnName: String,
  configuration: Configuration = DefaultConfiguration()
): BinaryExtension =
  extensions.create(extnName, BinaryExtension::class.java, this, extnName, configuration)
    .apply {
      project.afterEvaluate {
        registerXtrasTasks()
      }
    }


private fun BinaryExtension.registerXtrasTasks() {
  val srcConfig = sourceConfig

  when (srcConfig) {
    is ArchiveSourceConfig -> {
      registerArchiveDownloadTask(srcConfig)
    }

    is GitSourceConfig -> {
      registerGitDownloadTask(srcConfig)
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

  }

}




