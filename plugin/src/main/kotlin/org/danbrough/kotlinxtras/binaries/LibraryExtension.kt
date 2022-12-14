package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.*
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

@DslMarker
annotation class BinariesDSLMarker


typealias SourcesTask = Exec.(KonanTarget) -> Unit

@BinariesDSLMarker
fun Project.registerLibraryExtension(
  name: String,
  configure: LibraryExtension.() -> Unit
): LibraryExtension {
  val binaries = project.binariesExtension
  return extensions.create(name, LibraryExtension::class.java, this).apply {
    libName = name
    binaries.libraryExtensions.add(this)
    configure()
    project.afterEvaluate {
      registerXtrasTasks()
    }
  }
}

@BinariesDSLMarker
abstract class LibraryExtension(val project: Project) {

  //Unique identifier for a binary package
  @BinariesDSLMarker
  lateinit var libName: String

  @BinariesDSLMarker
  open var version: String = "unspecified"

  @BinariesDSLMarker
  open var sourceURL: String? = null

  @BinariesDSLMarker
  open var publishingGroup: String = "$XTRAS_PACKAGE.binaries"


  /**
   * This can be manually configured or by default it will be set to all the kotlin multi-platform targets.
   * If not a kotlin mpp project then it will be set to [xtrasSupportedTargets]
   */
  @BinariesDSLMarker
  open var supportedTargets: List<KonanTarget> = emptyList()

  /**
   * This can be manually configured or it will default to the [supportedTargets] filtered by whether they can
   * be built on the host platform.
   * ```kotlin supportedTargets.filter { it.family.isAppleFamily == HostManager.hostIsMac }```
   */

  @BinariesDSLMarker
  var supportedBuildTargets: List<KonanTarget> = emptyList()

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

  internal var cinteropsConfigTasks = mutableListOf<CInteropsConfig.() -> Unit>()

  @BinariesDSLMarker
  fun cinterops(configure: CInteropsConfig.() -> Unit) {
    cinteropsConfigTasks.add(configure)
  }

  internal var sourceConfig: SourceConfig? = null

  internal var configureTask: SourcesTask? = null

  internal var buildTask: SourcesTask? = null

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

  fun packageFileName(
    konanTarget: KonanTarget,
    name: String = libName,
    packageVersion: String = version
  ): String =
    "xtras_${name}_${konanTarget.platformName}_${packageVersion}.tar.gz"

  open fun sourcesDir(konanTarget: KonanTarget): File =
    project.xtrasDir.resolve("src/$libName/$version/${konanTarget.platformName}")

  open fun buildDir(
    konanTarget: KonanTarget,
    packageName: String = libName,
    packageVersion: String = version
  ): File =
    project.xtrasBuildDir.resolve("$packageName/$packageVersion/${konanTarget.platformName}")


  open fun libsDir(
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
    project.xtrasPackagesDir.resolve(packageFileName(konanTarget, name, packageVersion)).exists()


  open fun buildEnvironment(konanTarget: KonanTarget): Map<String, *> =
    konanTarget.buildEnvironment()

  val binaries: BinaryExtension
    inline get() = project.binariesExtension

}


private fun LibraryExtension.registerXtrasTasks() {
  val srcConfig = sourceConfig

  project.logger.info("LibraryExtension.registerXtrasTasks for $libName")

  if (supportedTargets.isEmpty()) {
    supportedTargets =
      project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.targets?.withType(
        KotlinNativeTarget::class.java
      )?.map { it.konanTarget } ?: xtrasSupportedTargets
  }

  if (supportedBuildTargets.isEmpty())
    supportedBuildTargets =
      if (HostManager.hostIsMac) supportedTargets.filter { it.family.isAppleFamily } else supportedTargets



  if (buildTask != null) {
    when (srcConfig) {
      is ArchiveSourceConfig -> {
        registerArchiveDownloadTask()
      }

      is GitSourceConfig -> {
        registerGitDownloadTask(srcConfig)
      }
    }
  }

  val publishing = project.extensions.findByType(PublishingExtension::class.java) ?: let {
    project.logger.info("LibraryExtension.registerXtrasTask() applying maven-publish.")
    project.pluginManager.apply("org.gradle.maven-publish")
    project.extensions.getByType(PublishingExtension::class.java)
  }


  project.repositories.findByName("xtras") ?: project.repositories.maven {
    name = "xtras"
    url = project.xtrasMavenDir.toURI()
  }

  publishing.repositories.findByName("xtras") ?: publishing.repositories.maven {
    name = "xtras"
    url = project.xtrasMavenDir.toURI()
  }

  val xtrasProvideAllTaskName = "xtrasProvideAll"
  val provideAllGlobalTask =
    project.tasks.findByName("xtrasProvideAll") ?: project.tasks.create(xtrasProvideAllTaskName) {
      group = XTRAS_TASK_GROUP
      description = "Provide all binaries from all LibraryExtensions"
    }

  val provideAllTargetsTask = project.tasks.create(
    provideAllBinariesTaskName()
  ) {
    group = XTRAS_TASK_GROUP
    description = "Provide all binaries from a LibraryExtension"
  }

  provideAllGlobalTask.dependsOn(provideAllTargetsTask)

  supportedTargets.forEach { target ->

    configureTargetTask?.invoke(target)

    if (buildTask != null && HostManager.hostIsMac == target.family.isAppleFamily) {

      //println("Adding build support for $libName with $target")

      when (srcConfig) {
        is ArchiveSourceConfig -> {
          registerArchiveExtractTask(srcConfig, target)
        }

        is GitSourceConfig -> {
          registerGitExtractTask(srcConfig, target)
        }
      }

      configureTask?.also {
        registerConfigureSourcesTask(target)
      }
      registerBuildSourcesTask(target)
      registerPublishingTask(target)
    } else {
      project.logger.info("buildSupport disabled for $libName as either buildTask is null or buildingEnabled is false")
    }

    provideAllTargetsTask.dependsOn(registerProvideBinariesTask(target))

    project.extensions.findByType(KotlinMultiplatformExtension::class)?.apply {
    /*  targets.withType(KotlinNativeTarget::class.java) {
        compilations["main"]
      }*/
      project.tasks.withType(KotlinNativeCompile::class.java) {
        val konanTarget = KonanTarget.Companion.predefinedTargets[target.toString()]!!
        dependsOn(provideBinariesTaskName(konanTarget))
      }

      project.tasks.withType(CInteropProcess::class.java) {
        dependsOn(provideBinariesTaskName(konanTarget))
      }
    }





  }

  registerGenerateInteropsTask()

}




