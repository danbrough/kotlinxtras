package org.danbrough.kotlinxtras.sonatype


import org.danbrough.kotlinxtras.xtrasDocsDir
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import java.net.URI


internal fun Project.configurePublishing(extn: SonatypeExtension) {

  extensions.findByType<PublishingExtension>()?.apply {
    logger.debug("configurePublishing - ${project.group}:${project.name}:${project.version}")

    if (extn.publishDocs) {
      if (plugins.hasPlugin("org.jetbrains.dokka")) {
        tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml").configure {
          outputDirectory.set(project.xtrasDocsDir)
        }

        val javadocJar by tasks.registering(Jar::class) {
          archiveClassifier.set("javadoc")
          from(tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml"))
        }

        publications.all {
          if (this is MavenPublication)
            artifact(javadocJar)
        }
      }
    }

    val kotlinMPP = project.extensions.findByType<KotlinMultiplatformExtension>()
    if (kotlinMPP == null) {
      //create a default sources jar from the main sources
      project.extensions.findByType<KotlinProjectExtension>()?.apply {
        sourceSets.findByName("main")?.kotlin?.also { srcDir->
          val sourcesJarTask = tasks.register("sourcesJar${name.capitalize()}", Jar::class.java) {
            archiveClassifier.set("sources")
            from(srcDir)
          }

          publications.all {
            if (this is MavenPublication)
              artifact(sourcesJarTask)
          }
        }
      }
    }

    if (extn.signPublications) {
      apply<SigningPlugin>()
      extensions.getByType<SigningExtension>().apply {
        publications.all {
          sign(this)
        }
      }
    }

    repositories {
      maven {
        name = "SonaType"
        url = URI(extn.publishingURL)
        credentials {
          username = extn.sonatypeUsername
          password = extn.sonatypePassword
        }
      }


      if (extn.localRepoEnabled) {
        maven {
          name = extn.localRepoName
          url = extn.localRepoLocation.toURI()
        }
      }
    }

    extn.configurePublishing(this, this@configurePublishing)
  }
}
