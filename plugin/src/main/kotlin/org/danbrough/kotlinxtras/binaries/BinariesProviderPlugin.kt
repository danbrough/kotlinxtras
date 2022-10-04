package org.danbrough.kotlinxtras.binaries

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType

data class BinaryDependency(val name:String)

open class BinariesProviderExtension {



  internal fun configure(project: Project) {
  }
}

class BinariesProviderPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    println("Applying binaries plugin to $target")

    val s: Configuration = target.configurations.create("stuff") {
      it.isTransitive = false
    }

    val extn =
      target.extensions.create("binariesProvider", BinariesProviderExtension::class.java).also {
        it.configure(target)
      }

    target.afterEvaluate { project ->
      project.extensions.getByType<PublishingExtension>().apply {
        publications.all {
          if (it !is MavenPublication) return@all
          println("PUBLICATION ${it.name}")
        }
      }
    }

  }
}