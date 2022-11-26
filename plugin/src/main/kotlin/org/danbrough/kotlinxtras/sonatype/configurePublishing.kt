package org.danbrough.kotlinxtras.sonatype


import org.danbrough.kotlinxtras.binaries.LibraryExtension
import org.danbrough.kotlinxtras.xtrasDocsDir
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import java.net.URI


internal fun Project.configurePublishing(extn: SonatypeExtension) {
  //println("configurePublishing - ${project.group}:${project.name}:${project.version}")

  extensions.findByType<PublishingExtension>()?.apply {

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
      val sourceSet = kotlinExtension.sourceSets.findByName("main")?.kotlin

      val sourcesJar = sourceSet?.let {
        tasks.register("sourcesJar${name.capitalize()}", Jar::class.java) {
          archiveClassifier.set("sources")
          from(it)
        }
      }

      sourcesJar?.also {
        publications.all {
          if (this is MavenPublication)
            artifact(it)
        }
      }
    }

    project.extensions.findByType<LibraryExtension>()?.also { xtras->
      if (xtras.buildTask != null){
        //need to be able to publish the binary archive
        xtras.konanTargets.forEach {

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


/*
tasks.dokkaHtml.configure {
  outputDirectory.set(buildDir.resolve("dokka"))
//  finalizedBy("copyDocs")
}

val javadocJar by tasks.registering(Jar::class) {
  archiveClassifier.set("javadoc")
  from(tasks.dokkaHtml)
}

val sourcesJar by tasks.registering(Jar::class) {
  archiveClassifier.set("sources")
  from(sourceSets["main"].allJava)
}


publishing {

  publications.all {
    group = project.group
    version = project.version
    if (this !is MavenPublication) return@all
    if (project.hasProperty("publishDocs"))
      artifact(javadocJar)
    artifact(sourcesJar)
    if (hasProperty("signPublications"))
      signing.sign(this)
  }

  repositories {
    maven("../build/m2"){
      name = "M2"
    }

    maven{
      name = "SonaType"
      url = uri("https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/${System.getenv("SONATYPE_REPO_ID")}")
      credentials{
        username = property("sonatypeUsername")?.toString()
        password = property("sonatypePassword")?.toString()
      }
    }
  }

  publications.all {
    if (this !is MavenPublication) return@all

    xtrasPom()
  }


}

 */
