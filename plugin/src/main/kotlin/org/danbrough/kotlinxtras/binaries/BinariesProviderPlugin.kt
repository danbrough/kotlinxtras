package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasTaskGroup
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.targets
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

data class BinaryDependency(val name: String)

open class BinariesProviderExtension {

  //Base name for the publications
  //Will default to the projects name
  lateinit var libName: String

  //KonanTargets for which to build binary archives for
  var supportedTargets = mutableListOf<KonanTarget>()

  //Where to store the archives.
  //Will default to $rootProject.buildDir/binaries/libName
  var archivesDir: File? = null

  internal fun configure(project: Project) {
    libName = project.name
  }
}

class BinariesProviderPlugin : Plugin<Project> {
  override fun apply(targetProject: Project) {
    println("Applying binaries plugin to $targetProject")

    val isMacHost = System.getProperty("os.name").startsWith("Mac")


    val s: Configuration = targetProject.configurations.create("stuff") {
      it.isTransitive = false
    }


    val extn =
      targetProject.extensions.create("binariesProvider", BinariesProviderExtension::class.java)
        .also {
          it.configure(targetProject)
        }

    targetProject.afterEvaluate { project ->

      val libName = extn.libName

      val archivesDir =
        extn.archivesDir ?: project.rootProject.buildDir.resolve("binaries/$libName")

      val supportedTargets =
        if (extn.supportedTargets.isEmpty())
          project.kotlinExtension.targets.filterIsInstance<KotlinNativeTarget>()
            .map { it.konanTarget } else extn.supportedTargets

      println("supported targets: $supportedTargets")

      project.extensions.getByType<PublishingExtension>().apply {

        //Support apple targets on mac host and everything else on what is assumed to be linux
        supportedTargets.filter { it.family.isAppleFamily == isMacHost }.forEach { target ->
          val jarName = "$libName${target.platformName.capitalized()}"

          val jarTask = project.tasks.register<Jar>("zip${jarName.capitalized()}Binaries") {
            archiveBaseName.set(jarName)
            dependsOn("build${target.platformName.capitalized()}")
            group = xtrasTaskGroup
            from(project.rootProject.fileTree("libs/$libName/${target.platformName}")) {
              include("include/**", "lib/*.so", "lib/*.a", "lib/*.dll", "lib/*.dylib")
            }
            into("$libName/${target.platformName}")
            destinationDirectory.set(archivesDir)
          }
        }
      }
    }

  }
}