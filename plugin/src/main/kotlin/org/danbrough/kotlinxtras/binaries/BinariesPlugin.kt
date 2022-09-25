@file:Suppress("PropertyName", "unused")

package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasTaskGroup
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Copy
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.KonanTarget

data class BinDep(val group: String?, val name: String, val version: String?)

val defaultSupportedTargets = mutableSetOf(
  KonanTarget.LINUX_X64,
  KonanTarget.LINUX_ARM64,
  KonanTarget.LINUX_ARM32_HFP,
  KonanTarget.ANDROID_ARM64,
  KonanTarget.ANDROID_ARM32,
  KonanTarget.ANDROID_X64,
  KonanTarget.ANDROID_X86,
  KonanTarget.MACOS_ARM64,
  KonanTarget.MACOS_X64,
)

open class BinariesExtension {
  internal var binDeps: MutableSet<BinDep> = mutableSetOf()

  var message:String = "Hello World"

  fun enableCurl(version: String = "curl-7_85_0_a") {
    binDeps.add(BinDep("org.danbrough.kotlinxtras", "curl", version))
  }

  fun enableOpenSSL(version: String = "OpenSSL_1_1_1q_a") {
    binDeps.add(BinDep("org.danbrough.kotlinxtras", "openssl", version))
  }

  var taskToPlatformName: (Task) -> String? = { task ->
    when (task) {
      is KotlinNativeTest -> task.targetName!!
      is KotlinNativeCompile -> task.target
      else -> null
    }
  }
}


fun Project.configurePrecompiledBinaries() {
  val binaries = project.extensions.getByType(BinariesExtension::class)

  val preCompiled: Configuration by configurations.creating {
    isTransitive = false
  }

  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
  }

  val konanTargets =
    project.extensions.getByType<KotlinMultiplatformExtension>().targets.withType<KotlinNativeTarget>()
      .map { it.konanTarget }.distinct()

  dependencies {
    binaries.binDeps.forEach { binDep ->
      konanTargets.forEach { target ->
        val binDepLib =
          "${binDep.group}:${binDep.name}${target.platformName.capitalized()}Binaries:${binDep.version}"
        project.logger.info("Adding binary support with $binDepLib")
        preCompiled(binDepLib)
      }
    }
  }

  preCompiled.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
    tasks.register<Copy>("extract${artifact.name.capitalized()}") {
      group = xtrasTaskGroup
      from(zipTree(artifact.file).matching {
        exclude("**/META-INF")
        exclude("**/META-INF/*")
      })
      into(project.rootProject.buildDir.resolve("kotlinxtras"))
    }
  }

  binaries.binDeps.forEach { binDep ->
    project.tasks.forEach { task ->
      binaries.taskToPlatformName(task)?.also { platformName ->
        project.logger.info("adding extract dependency on ${binDep.name} for $platformName")
        task.dependsOn("extract${binDep.name.capitalized()}${platformName.capitalized()}Binaries")
      }
    }
  }
}

class BinariesPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val binaries = project.extensions.create("binaries", BinariesExtension::class.java)
    val kotlinMPP = project.extensions.findByType<KotlinMultiplatformExtension>()
    println("PROJECT $project MPP: $kotlinMPP")
  }
}
