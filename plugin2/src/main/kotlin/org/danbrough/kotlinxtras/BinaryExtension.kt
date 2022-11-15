package org.danbrough.kotlinxtras

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

const val KOTLIN_XTRAS_DIR_NAME = "kotlinxtras"
const val XTRAS_TASK_GROUP = "xtras"

@DslMarker
//@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE,AnnotationTarget.FUNCTION)
annotation class BinariesDSLMarker

interface BuildEnv {
  var gitBinary: String
  var wgetBinary: String
  var tarBinary: String
}

typealias DownloadSourcesTask = TaskProvider<*>

object DefaultBuildEnv : BuildEnv {
  override var gitBinary: String = "/usr/bin/git"
  override var wgetBinary: String = "/usr/bin/wget"
  override var tarBinary: String = "/usr/bin/tar"
}

@BinariesDSLMarker
open class BinaryExtension(
  val project: Project,
//Unique identifier for a binary package
  var libName: String,
  var buildEnvironment: BuildEnv
) {

  open var version: String = "unspecified"

  open var sourceURL: String? = null

  lateinit var xtrasDir: File

  open fun gitRepoDir(): File = downloadsDir.resolve("repos/$libName")

  lateinit var downloadsDir: File

  lateinit var packagesDir: File

  internal var sourceConfig: SourceConfig? = null

  val downloadSourcesTaskName: String
    get() = "xtrasDownloadSources${libName.capitalized()}"

  fun extractSourcesTaskName(konanTarget: KonanTarget): String =
    "xtrasExtractSources${libName.capitalized()}${konanTarget.platformName.capitalized()}"

  open fun sourcesDir(konanTarget: KonanTarget): File =  xtrasDir.resolve("src/$libName/$version/${konanTarget.platformName}")

  val konanTargets: Set<KonanTarget>
    get() = project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.targets?.withType(
      KotlinNativeTarget::class.java
    )?.map { it.konanTarget }?.toSet() ?: emptySet()

  internal fun configureDefaults() {

    if (!::xtrasDir.isInitialized) xtrasDir =
      project.rootProject.buildDir.resolve(KOTLIN_XTRAS_DIR_NAME)

    if (!::downloadsDir.isInitialized)
      downloadsDir = xtrasDir.resolve("downloads")

    if (!::packagesDir.isInitialized)
      packagesDir = xtrasDir.resolve("packages")


  }

}

fun Project.registerBinariesExtension(extnName: String): BinaryExtension =
  extensions.create(extnName, BinaryExtension::class.java, this, extnName, DefaultBuildEnv)
    .apply {
      project.afterEvaluate {
        configureDefaults()
        registerXtrasTargets()
      }
    }


private fun BinaryExtension.registerXtrasTargets() {
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
        //TODO
      }
    }
  }


}




