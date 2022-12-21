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
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

@DslMarker
annotation class XtrasDSLMarker


typealias SourcesTask = Exec.(KonanTarget) -> Unit

@XtrasDSLMarker
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

@XtrasDSLMarker
abstract class LibraryExtension(val project: Project) {

  //Unique identifier for a binary package
  @XtrasDSLMarker
  lateinit var libName: String

  @XtrasDSLMarker
  open var version: String = "unspecified"

  @XtrasDSLMarker
  open var sourceURL: String? = null

  @XtrasDSLMarker
  var publishingGroup: String = project.group.toString()

  @XtrasDSLMarker
  var isBuildingEnabled: Boolean = true


  /**
   * This can be manually configured or by default it will be set to all the kotlin multi-platform targets.
   * If not a kotlin mpp project then it will be set to [xtrasSupportedTargets]
   */
  @XtrasDSLMarker
  open var supportedTargets: List<KonanTarget> = emptyList()

  /**
   * This can be manually configured or it will default to the [supportedTargets] filtered by whether they can
   * be built on the host platform.
   * ```kotlin supportedTargets.filter { it.family.isAppleFamily == HostManager.hostIsMac }```
   */

  @XtrasDSLMarker
  var supportedBuildTargets: List<KonanTarget> = emptyList()

  open fun gitRepoDir(): File = project.xtrasDownloadsDir.resolve("repos/$libName")

  @XtrasDSLMarker
  fun configure(task: SourcesTask) {
    configureTask = task
  }

  @XtrasDSLMarker
  fun build(task: SourcesTask) {
    buildTask = task
  }

  @XtrasDSLMarker
  fun configureTarget(configure: (KonanTarget) -> Unit) {
    configureTargetTask = configure
  }

  internal var cinteropsConfigTasks = mutableListOf<CInteropsConfig.() -> Unit>()

  @XtrasDSLMarker
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

  fun resolveArchiveTaskName(konanTarget: KonanTarget,name:String = libName):String =
  "xtrasResolveArchive${name.capitalized()}${konanTarget.platformName.capitalized()}"

  fun extractLibsTaskName(konanTarget: KonanTarget,name:String = libName):String =
    "xtrasExtractLibs${name.capitalized()}${konanTarget.platformName.capitalized()}"

  fun createArchiveTaskName(konanTarget: KonanTarget,name:String = libName):String =
    "xtrasCreateArchive${name.capitalized()}${konanTarget.platformName.capitalized()}"

  fun archiveFile(target:KonanTarget,name:String = libName):File =  project.xtrasPackagesDir.resolve(packageFileName(target,name))
/*
  fun provideBinariesTaskName(konanTarget: KonanTarget, name: String = libName): String =
    "xtrasProvide${name.capitalized()}${konanTarget.platformName.capitalized()}"
*/

/*  fun packageTaskName(konanTarget: KonanTarget, name: String = libName): String =
    "xtrasPackage${name.capitalized()}${konanTarget.platformName.capitalized()}"*/

  fun generateCInteropsTaskName(name: String = libName): String =
    "xtrasGenerateCInterops${name.capitalized()}"

  fun packageFileName(
    konanTarget: KonanTarget,
    name: String = libName,
    packageVersion: String = version
  ): String = packageFileName.invoke(konanTarget, name, packageVersion)

  var packageFileName: (
    konanTarget: KonanTarget,
    name: String,
    packageVersion: String
  ) -> String =
    { konanTarget, name, packageVersion -> "xtras_${name}_${konanTarget.platformName}_${packageVersion}.tar.gz" }

  var sourcesDir: (KonanTarget) -> File =
    { project.xtrasDir.resolve("src/$libName/$version/${it.platformName}") }

  @XtrasDSLMarker
  fun sourcesDir(srcDir: (KonanTarget) -> File) {
    sourcesDir = srcDir
  }

  fun buildDir(
    konanTarget: KonanTarget,
    packageName: String = libName,
    packageVersion: String = version
  ): File =
    buildDir.invoke(konanTarget, packageName, packageVersion)

  var buildDir: (
    konanTarget: KonanTarget,
    packageName: String,
    packageVersion: String
  ) -> File = { konanTarget, packageName, packageVersion ->
    project.xtrasBuildDir.resolve("$packageName/$packageVersion/${konanTarget.platformName}")
  }

  @XtrasDSLMarker
  fun libsDir(
    konanTarget: KonanTarget,
    packageName: String = libName,
    packageVersion: String = version
  ): File = libsDir.invoke(konanTarget, packageName, packageVersion)

  var libsDir: (konanTarget: KonanTarget, packageName: String, packageVersion: String) -> File =
    { konanTarget, packageName, packageVersion -> project.xtrasLibsDir.resolve("$packageName/$packageVersion/${konanTarget.platformName}") }

  fun isPackageBuilt(
    konanTarget: KonanTarget,
    name: String = libName,
    packageVersion: String = version
  ): Boolean = isPackageBuilt.invoke(konanTarget, name, packageVersion)

  var isPackageBuilt: (
    konanTarget: KonanTarget,
    name: String,
    packageVersion: String
  ) -> Boolean =
    { konanTarget, name, packageVersion ->
      project.xtrasPackagesDir.resolve(
        packageFileName(
          konanTarget,
          name,
          packageVersion
        )
      ).exists()
    }


  open fun buildEnvironment(konanTarget: KonanTarget): Map<String, *> =
    konanTarget.buildEnvironment()

  val binaries: BinaryExtension
    inline get() = project.binariesExtension

  fun addBuildFlags(target: KonanTarget, env: MutableMap<String, Any>) {
    env["CFLAGS"] = "${env["CFLAGS"] ?: ""} -I${libsDir(target)}/include"
    env["LDFLAGS"] = "${env["LDFLAGS"] ?: ""} -L${libsDir(target)}/lib"
  }

}


private fun LibraryExtension.registerXtrasTasks() {
  val srcConfig = sourceConfig

  project.log("LibraryExtension.registerXtrasTasks for $libName")

  if (supportedTargets.isEmpty()) {
    supportedTargets =
      project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.targets?.withType(
        KotlinNativeTarget::class.java
      )?.map { it.konanTarget } ?: xtrasSupportedTargets
  }

  if (supportedBuildTargets.isEmpty())
    supportedBuildTargets =
      if (HostManager.hostIsMac) supportedTargets.filter { it.family.isAppleFamily } else supportedTargets



  when (srcConfig) {
    is ArchiveSourceConfig -> {
      registerArchiveDownloadTask()
    }

    is GitSourceConfig -> {
      registerGitDownloadTask(srcConfig)
    }
  }


  val publishing = project.extensions.findByType(PublishingExtension::class.java) ?: let {
    project.log("LibraryExtension.registerXtrasTask() applying maven-publish.")
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

        is DirectorySourceConfig -> {
          registerDirectorySourcesTask(srcConfig, target)
        }
      }

      registerBuildTasks(target)
      registerArchiveTasks(target)

//      registerPublishingTasks(target)
    } else {
      project.log("buildSupport disabled for $libName:${target.platformName}")
    }

/*    if (HostManager.hostIsMac == target.family.isAppleFamily)
      provideAllTargetsTask.dependsOn(registerProvideBinariesTask(target))*/

    project.extensions.findByType(KotlinMultiplatformExtension::class)?.apply {
      /*  targets.withType(KotlinNativeTarget::class.java) {
          compilations["main"]
        }*/
      /*     project.tasks.withType(KotlinNativeCompile::class.java) {
             val konanTarget = KonanTarget.Companion.predefinedTargets[target.toString()]!!
             //dependsOn(provideBinariesTaskName(konanTarget))
           }
     */
      project.tasks.withType(CInteropProcess::class.java) {
        dependsOn(extractLibsTaskName(konanTarget))
      }


    }


  }

  registerGenerateInteropsTask()
}




