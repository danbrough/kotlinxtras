@file:Suppress("PropertyName", "unused")

package org.danbrough.kotlinxtras

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Copy
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.internal.component.external.model.ComponentVariant
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
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
)

open class XtrasExtension {
  internal var binDeps: MutableSet<BinDep> = mutableSetOf()


  fun enableCurl(version: String = "curl-7_85_0") {
    binDeps.add(BinDep("org.danbrough.kotlinxtras", "curl", version))
  }

  fun enableOpenSSL(version: String = "OpenSSL_1_1_1q") {
    binDeps.add(BinDep("org.danbrough.kotlinxtras", "openssl", version))
  }
}


fun Project.configurePrecompiledBinaries() {
  val xtras = project.extensions.getByType(XtrasExtension::class)

  val preCompiled: Configuration by configurations.creating {
    isTransitive = false
  }

  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
  }

  val konanTargets = project.extensions.getByType<KotlinMultiplatformExtension>().targets.withType<KotlinNativeTarget>()
    .map { it.konanTarget }.distinct()

  dependencies {
    xtras.binDeps.forEach { binDep ->
      konanTargets.forEach { target ->
        val binDepLib =
          "${binDep.group}:${binDep.name}${target.platformName.capitalized()}Binaries:${binDep.version}"
        project.logger.log(LogLevel.INFO, "Adding binary support with $binDepLib")
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

  xtras.binDeps.forEach { binDep ->
    project.tasks.withType(KotlinNativeCompile::class).forEach {
      val konanTarget = KonanTarget.predefinedTargets[it.target]!!
      it.dependsOn("extract${binDep.name.capitalized()}${konanTarget.platformName.capitalized()}Binaries")
    }
  }
}

class XtrasPlugin : Plugin<Project> {

  override fun apply(project: Project) {

    project.extensions.create("xtras", XtrasExtension::class.java)


  }
}
