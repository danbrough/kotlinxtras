package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_PACKAGE
import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.buildEnvironment
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasBuildDir
import org.danbrough.kotlinxtras.xtrasDir
import org.danbrough.kotlinxtras.xtrasDownloadsDir
import org.danbrough.kotlinxtras.xtrasLibsDir
import org.danbrough.kotlinxtras.xtrasMavenDir
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.danbrough.kotlinxtras.xtrasSupportedTargets
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
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
) {
  project.binariesExtension
  extensions.create(name, type, this)
  extensions.configure<T>(name) {
    /*plugins.apply("$XTRAS_PACKAGE.binaries")
  println("Xtras: CREATED: $extnName")*/
    configure()
    project.afterEvaluate {
      registerXtrasTasks()
    }
  }
}

@BinariesDSLMarker
abstract class LibraryExtension(
  val project: Project,
//Unique identifier for a binary package
  var libName: String
) {

  @BinariesDSLMarker
  open var version: String = "unspecified"

  @BinariesDSLMarker
  open var sourceURL: String? = null

  @BinariesDSLMarker
  open var publishingGroup: String = "$XTRAS_PACKAGE.binaries"


  @BinariesDSLMarker
  open var buildEnabled: Boolean = false


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
  val supportedBuildTargets: List<KonanTarget> = emptyList()

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
    project.xtrasPackagesDir.resolve(packageFile(konanTarget, name, packageVersion)).exists()


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
    supportedTargets = supportedTargets.filter { it.family.isAppleFamily == HostManager.hostIsMac }


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

  val publishing = project.extensions.findByType(PublishingExtension::class.java) ?: let {
    project.logger.info("LibraryExtension.registerXtrasTask() applying maven-publish.")
    project.pluginManager.apply("org.gradle.maven-publish")
    project.extensions.getByType(PublishingExtension::class.java)
  }


  project.repositories.findByName("xtras") ?:
  project.repositories.maven {
    name = "xtras"
    url = project.xtrasMavenDir.toURI()
  }

  publishing.repositories.findByName("xtras") ?:
  publishing.repositories.maven {
    name = "xtras"
    url = project.xtrasMavenDir.toURI()
  }

  val xtrasProvideAllTaskName = "xtrasProvideAll"
  val provideAllGlobalTask = project.tasks.findByName("xtrasProvideAll") ?: project.tasks.create(xtrasProvideAllTaskName){
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

    if (buildTask != null && buildEnabled && HostManager.hostIsMac == target.family.isAppleFamily) {

      //println("Adding build support for $libName with $target")

      when (srcConfig) {
        is ArchiveSourceConfig -> {
          println("registering archive extract task")
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
      //registerCopyPackageToLibsTask(target)
      registerPublishingTask(target)
    } else {
      project.logger.info("buildSupport disabled for $libName as either buildTask is null or buildingEnabled is false")
    }

    provideAllTargetsTask.dependsOn(registerProvideBinariesTask(target))

  }

  registerGenerateInteropsTask()

}




