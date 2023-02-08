package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasMavenDir
import org.danbrough.kotlinxtras.xtrasSupportedTargets
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

internal fun LibraryExtension.registerXtrasTasks() {
  val srcConfig = sourceConfig

  project.log("LibraryExtension.registerXtrasTasks for $libName")



  if (supportedTargets.isEmpty()) {
    supportedTargets =
      project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.targets?.withType(
        KotlinNativeTarget::class.java
      )?.map { it.konanTarget }
        ?: xtrasSupportedTargets
  }

  if (supportedBuildTargets.isEmpty()) supportedBuildTargets =
    if (HostManager.hostIsMac) supportedTargets.filter { it.family.isAppleFamily } else supportedTargets

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

  if (enableBuilding) {
    when (srcConfig) {
      is ArchiveSourceConfig -> {
        registerArchiveDownloadTask()
      }

      is GitSourceConfig -> {
        registerGitDownloadTask(srcConfig)
      }
    }
  }


  supportedTargets.forEach { target ->


    registerResolveArchiveTask(target)
    configureTargetTask?.invoke(target)

    if (enableBuilding) {

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
        //registerArchiveTasks(target)


//      registerPublishingTasks(target)
      } else {
        project.log("buildSupport disabled for $libName:${target.platformName}")
      }
    }


  }


  registerGenerateInteropsTask()


  /*    if (HostManager.hostIsMac == target.family.isAppleFamily)
        provideAllTargetsTask.dependsOn(registerProvideBinariesTask(target))*/

  project.extensions.findByType(KotlinMultiplatformExtension::class)?.apply {

    /*  targets.withType(KotlinNativeTarget::class.java) {
        compilations["main"]
      }*//*     project.tasks.withType(KotlinNativeCompile::class.java) {
               val konanTarget = KonanTarget.Companion.predefinedTargets[target.toString()]!!
               //dependsOn(provideBinariesTaskName(konanTarget))
             }
       */
    project.tasks.withType(CInteropProcess::class.java) {
      dependsOn(extractLibsTaskName(konanTarget))
    }
  }


  project.tasks.withType(KotlinNativeTest::class.java).all {
    val ldLibKey = if (HostManager.hostIsMac) "DYLD_LIBRARY_PATH" else "LD_LIBRARY_PATH"
    val konanTarget = if (HostManager.hostIsMac) KonanTarget.MACOS_X64 else KonanTarget.LINUX_X64
    val libPath = environment[ldLibKey]
    val newLibPath = (libPath?.let { "$it${File.pathSeparator}" }
      ?: "") + project.binariesExtension.libraryExtensions.map {
      it.libsDir(konanTarget).resolve("lib")
    }.joinToString(File.pathSeparator)
    // println("$ldLibKey = $newLibPath")
    environment(ldLibKey, newLibPath)
  }

}
