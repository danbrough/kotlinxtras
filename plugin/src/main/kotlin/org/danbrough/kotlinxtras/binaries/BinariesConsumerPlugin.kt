@file:Suppress("PropertyName", "unused")

package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.platformName
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

const val DEFAULT_CURL_VERSION = "curl-7_85_0"
const val DEFAULT_OPENSSL_VERSION = "OpenSSL_1_1_1q"
const val DEFAULT_SQLITE_VERSION = "3.39.4"
const val DEFAULT_ICONV_VERSION = "1.17_01"

data class BinDep(val group: String?, val name: String, val version: String?)

open class BinariesExtension(private val project: Project) {
  internal var binDeps: MutableSet<BinDep> = mutableSetOf()

  private val projectGroup = "org.danbrough.kotlinxtras"

  fun enableCurl(version: String = DEFAULT_CURL_VERSION) {
    binDeps.add(BinDep(projectGroup, "curl", version))
  }

  fun enableOpenSSL(version: String = DEFAULT_OPENSSL_VERSION) {
    binDeps.add(BinDep(projectGroup, "openssl", version))
  }

  fun enableSqlite(version: String = DEFAULT_SQLITE_VERSION) {
    binDeps.add(BinDep(projectGroup, "sqlite", version))
  }
  fun enableIconv(version: String = DEFAULT_ICONV_VERSION) {
    binDeps.add(BinDep(projectGroup, "iconv", version))
  }
  fun addBinaryDependency(binDep: BinDep) {
    binDeps.add(binDep)
  }

  var taskToPlatformName: (Task) -> String? = { task ->
    when (task) {
      is KotlinNativeTest -> task.targetName!!
      is KotlinNativeCompile -> task.target
      else -> null
    }
  }
}


class BinariesConsumerPlugin : Plugin<Project> {

  override fun apply(targetProject: Project) {

    targetProject.extensions.create("binaries", BinariesExtension::class.java, targetProject)
    val preCompiled: Configuration by targetProject.configurations.creating {
      isTransitive = false
    }

    targetProject.afterEvaluate {
      configureBinaries()
    }
  }

}

private fun Project.configureBinaries() {
  afterEvaluate {


    val binaries =
      project.rootProject.extensions.findByType<BinariesExtension>() ?: return@afterEvaluate


    val mppExtension =
      project.extensions.findByType<KotlinMultiplatformExtension>() ?: return@afterEvaluate

    val konanTargets = mppExtension.targets.withType<KotlinNativeTarget>()
      .map { it.konanTarget }.distinct()

    //println("KONANTARGETS: $konanTargets")

    val preCompiled by project.configurations.getting


    val localBinariesDir = project.rootProject.buildDir.resolve("kotlinxtras")

    project.dependencies {
      binaries.binDeps.forEach { binDep ->
        println("BIN DEP: $binDep")
        konanTargets.forEach { target ->
          val binDepLib =
            "${binDep.group}.${binDep.name}:${binDep.name}${target.platformName.capitalized()}Binaries:${binDep.version}"
          project.logger.log(LogLevel.INFO, "Adding binary support with $binDepLib")
          preCompiled(binDepLib)
        }
      }
    }


    preCompiled.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
      project.tasks.register<Copy>("extract${artifact.name.capitalized()}") {
        from(project.zipTree(artifact.file).matching {
          exclude("**/META-INF")
          exclude("**/META-INF/*")
        })
        into(localBinariesDir)
      }
    }


    binaries.binDeps.forEach { binDep ->
      project.tasks.withType(KotlinNativeCompile::class).forEach {
        project.logger.log(
          LogLevel.INFO,
          "adding extract dependency on ${binDep.name} for ${it.name}"
        )
        val konanTarget = KonanTarget.predefinedTargets[it.target]!!
        it.dependsOn("extract${binDep.name.capitalized()}${konanTarget.platformName.capitalized()}Binaries")
      }
    }
  }
}
