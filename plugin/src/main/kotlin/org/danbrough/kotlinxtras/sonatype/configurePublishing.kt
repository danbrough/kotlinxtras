package org.danbrough.kotlinxtras.sonatype


import org.danbrough.kotlinxtras.projectProperty
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
    logger.info("configurePublishing - ${project.group}:${project.name}:${project.version}")

    /*
        const val SONATYPE_TASK_GROUP = "sonatype"
    const val REPOSITORY_ID = "sonatypeRepoId"
    const val PROFILE_ID = "sonatypeProfileId"
    const val DESCRIPTION = "sonatypeDescription"
    const val USERNAME = "sonatypeUsername"
    const val PASSWORD = "sonatypePassword"
    const val PUBLISH_DOCS = "publishDocs"
    const val SIGN_APPLICATIONS = "signApplications"
     */

    extn.sonatypeRepoId = project.projectProperty(SonatypeExtension.REPOSITORY_ID, null)
    extn.sonatypeProfileId = project.projectProperty(SonatypeExtension.PROFILE_ID)
    extn.sonatypeUsername = project.projectProperty(SonatypeExtension.USERNAME)
    extn.sonatypePassword = project.projectProperty(SonatypeExtension.PASSWORD)
    extn.publishDocs = project.projectProperty(SonatypeExtension.PUBLISH_DOCS, false)
    extn.signPublications = project.projectProperty(SonatypeExtension.SIGN_PUBLICATIONS, false)

    extn.configurePublishing(this, this@configurePublishing)

    val publishingURL = if (extn.sonatypeSnapshot)
      "${extn.sonatypeUrlBase}/content/repositories/snapshots/"
    else if (!extn.sonatypeRepoId.isNullOrBlank())
      "${extn.sonatypeUrlBase}/service/local/staging/deployByRepositoryId/${extn.sonatypeRepoId}"
    else
      "${extn.sonatypeUrlBase}/service/local/staging/deploy/maven2/"


    logger.info("SonatypeExtension::publishingURL $publishingURL repoID is: ${extn.sonatypeRepoId}")


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
        sourceSets.findByName("main")?.kotlin?.also { srcDir ->
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
        url = URI(publishingURL)
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

  }
}
