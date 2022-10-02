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

const val DEFAULT_CURL_VERSION = "curl-7_85_0_a"
const val DEFAULT_OPENSSL_VERSION = "OpenSSL_1_1_1q_a"

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

  var message: String = "Hello World"

  fun enableCurl(version: String = DEFAULT_CURL_VERSION) {
    binDeps.add(BinDep("org.danbrough.kotlinxtras", "curl", version))
  }

  fun enableOpenSSL(version: String = DEFAULT_OPENSSL_VERSION) {
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
    maven("/usr/local/kotlinxtras/build/m2")
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

private fun Project.configureTask(task: Task) {
  val extn = rootProject.extensions.getByType<BinariesExtension>()

  val platformName = if (task is KotlinNativeCompile) {
    println("KotlinNativeCompile: ${task.target}")
    KonanTarget.predefinedTargets[task.target]?.platformName
  } else if (task is KotlinNativeTest) {
    println("KotlinNativeTest target: ${task.targetName}")
    task.targetName!!
  } else return

  println("TARGET: $platformName")
  extn.binDeps.forEach {
    println("making task ${task.name} dependent on ${"extract${it.name.capitalized()}${platformName?.capitalized()}Binaries"}")
    task.dependsOn("extract${it.name.capitalized()}${platformName?.capitalized()}Binaries")
  }


}

fun Project.configureBinariesTaskDeps() {
  afterEvaluate { p ->
    p.tasks.forEach {
      p.configureTask(it)
    }
    p.childProjects.values.forEach {
      it.configureBinariesTaskDeps()
    }
  }
}

class BinariesPlugin : Plugin<Project> {

  override fun apply(project: Project) {

    if (project == project.rootProject) {
      println("FOUND ROOT PROJECT")
      project.extensions.create("binaries", BinariesExtension::class.java)
      return
    }

    println("PROJECT ${project.name} TASKS: ${project.tasks.names}")

    val binaries = project.rootProject.extensions.getByType<BinariesExtension>()


    val preCompiled: Configuration by project.configurations.creating {
      isTransitive = false
    }

    val mppExtension =
      project.extensions.findByType<KotlinMultiplatformExtension>() ?: return

    val konanTargets = mppExtension.targets.withType<KotlinNativeTarget>()
      .map { it.konanTarget }.distinct()

    println("KONANTARGETS: $konanTargets")

    project.dependencies {
      binaries.binDeps.forEach { binDep ->
        println("BIN DEP: $binDep")
        konanTargets.forEach { target ->
          val binDepLib =
            "${binDep.group}:${binDep.name}${target.platformName.capitalized()}Binaries:${binDep.version}"
          project.logger.log(LogLevel.INFO, "Adding binary support with $binDepLib")
          preCompiled(binDepLib)
        }
      }
    }


    preCompiled.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
      println("RESOLVED ARTIFACT: $artifact")
      project.tasks.register<Copy>("extract${artifact.name.capitalized()}") {
        group = xtrasTaskGroup

        from(project.zipTree(artifact.file).matching {
          exclude("**/META-INF")
          exclude("**/META-INF/*")
        })
        into(project.rootProject.buildDir.resolve("kotlinxtras"))
      }
    }
  }

}
