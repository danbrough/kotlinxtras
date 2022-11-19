package org.danbrough.kotlinxtras.sonatype


import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import java.net.URI


internal fun Project.configurePublishing(extn: SonatypeExtension) {
  println("configuring ${project.name} - ${project.group}:{project.version}")

    extensions.findByType<PublishingExtension>()?.apply {
      if (extn.signPublications) {
        apply<SigningPlugin>()
        extensions.getByType<SigningExtension>().apply {
          publications.all {
            sign(this)
          }
        }
      }

      if (plugins.hasPlugin("org.jetbrains.dokka")){
        val dokkaTask =tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml")
        dokkaTask.configure {
          outputDirectory.set(extn.dokkaDir)
        }

        val sourcesJar by tasks.registering(Jar::class) {
          archiveClassifier.set("sources")
          from(kotlinExtension.sourceSets["main"].kotlin)
        }

        val javadocJar by tasks.registering(Jar::class) {
          archiveClassifier.set("javadoc")
          from(dokkaTask)
        }

        publications.all {
          this as MavenPublication
          artifact(javadocJar)
          artifact(sourcesJar)
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

        if (extn.localRepoEnabled){
          maven {
            name = extn.localRepoName
            url = extn.localRepoLocation.toURI()
          }
        }
      }

      extn.configurePublishing(this,this@configurePublishing)
    }
    childProjects.forEach {
      it.value.configurePublishing(extn)
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
