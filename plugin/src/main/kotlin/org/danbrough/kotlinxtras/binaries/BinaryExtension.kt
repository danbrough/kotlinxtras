package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.buildEnvironment
import org.danbrough.kotlinxtras.platformName
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

interface Configuration {
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
open class BinaryExtension(
  val project: Project,
//Unique identifier for a binary package
  var libName: String,
  var configuration: Configuration= DefaultConfiguration()
) {

  open var version: String = "unspecified"

  open var sourceURL: String? = null

  lateinit var xtrasDir: File

  open fun gitRepoDir(): File = downloadsDir.resolve("repos/$libName")

  lateinit var downloadsDir: File

  lateinit var packagesDir: File

  internal var sourceConfig: SourceConfig? = null

  internal var configureTask: SourcesTask? = null

  internal var buildTask: SourcesTask? = null

  val downloadSourcesTaskName: String
    get() = "xtrasDownloadSources${libName.capitalized()}"

  fun extractSourcesTaskName(konanTarget: KonanTarget): String =
    "xtrasExtractSources${libName.capitalized()}${konanTarget.platformName.capitalized()}"

  fun configureSourcesTaskName(konanTarget: KonanTarget): String =
    "xtrasConfigureSources${libName.capitalized()}${konanTarget.platformName.capitalized()}"

  fun buildSourcesTaskName(konanTarget: KonanTarget): String =
    "xtrasBuildSources${libName.capitalized()}${konanTarget.platformName.capitalized()}"

  open fun sourcesDir(konanTarget: KonanTarget): File =
    xtrasDir.resolve("src/$libName/$version/${konanTarget.platformName}")

  open fun prefixDir(konanTarget: KonanTarget): File =
    xtrasDir.resolve("$libName/$version/${konanTarget.platformName}")

  val konanTargets: Set<KonanTarget>
    get() = project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.targets?.withType(
      KotlinNativeTarget::class.java
    )?.map { it.konanTarget }?.toSet() ?: emptySet()


  open fun buildEnvironment(konanTarget: KonanTarget): Map<String,*> = konanTarget.buildEnvironment()

  internal fun configureDefaults() {

    if (!::xtrasDir.isInitialized) xtrasDir =
      project.rootProject.buildDir.resolve(KOTLIN_XTRAS_DIR_NAME)

    if (!::downloadsDir.isInitialized)
      downloadsDir = xtrasDir.resolve("downloads")

    if (!::packagesDir.isInitialized)
      packagesDir = xtrasDir.resolve("packages")

  }

}

fun Project.registerBinariesExtension(extnName: String,configuration: Configuration = DefaultConfiguration()): BinaryExtension =
  extensions.create(extnName, BinaryExtension::class.java, this, extnName, configuration)
    .apply {
      project.afterEvaluate {
        configureDefaults()
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
    }
  }

}




