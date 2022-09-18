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

open class XtrasExtension {

  var autoExtractBinaries: Boolean = true
}

val supportedTargets = listOf(
  KonanTarget.LINUX_X64,
  KonanTarget.LINUX_ARM64,
  KonanTarget.LINUX_ARM32_HFP,
  KonanTarget.ANDROID_ARM64,
  KonanTarget.ANDROID_ARM32,
  KonanTarget.ANDROID_X64,
  KonanTarget.ANDROID_X86,
)

data class LibraryDependency(val group: String?, val name: String, val version: String?)

val Dependency.libraryDependency: LibraryDependency
  get() = LibraryDependency(group, name, version)


fun Project.configurePrecompiledBinaries() {

  val xtras = project.extensions.getByType(XtrasExtension::class)


  val preCompiled: Configuration by configurations.creating {
    isTransitive = false
  }

  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
  }

  val deps =
    configurations.flatMap { it.incoming.dependencies }.map { it.libraryDependency }.filter {
      it.group == "org.danbrough.kotlinxtras" &&
          (it.name == "curl" || it.name == "openssl")
    }.distinct()

  dependencies {
    deps.forEach { binDep ->
      supportedTargets.forEach { target ->
        val binDepLib =
          "org.danbrough.kotlinxtras:${binDep.name}${target.platformName.capitalized()}Binaries:${binDep.version}"
        project.logger.log(LogLevel.INFO, "Adding binary support with $binDepLib")
        preCompiled(binDepLib)
      }
    }
  }

  preCompiled.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
    tasks.register<Copy>("unzip${artifact.name.capitalized()}") {
      group = xtrasTaskGroup
      from(zipTree(artifact.file).matching {
        exclude("**/META-INF")
        exclude("**/META-INF/*")
      })
      into(project.rootProject.buildDir.resolve("kotlinxtras"))
    }
  }

  if (xtras.autoExtractBinaries) {
    deps.forEach { dep ->
      project.tasks.withType(KotlinNativeCompile::class).forEach {
        val konanTarget = KonanTarget.predefinedTargets[it.target]!!
        it.dependsOn("unzip${dep.name.capitalized()}${konanTarget.platformName.capitalized()}Binaries")
      }
    }
  }

}

class XtrasPlugin : Plugin<Project> {

  override fun apply(project: Project) {

    project.extensions.create("xtras", XtrasExtension::class.java)


  }
}