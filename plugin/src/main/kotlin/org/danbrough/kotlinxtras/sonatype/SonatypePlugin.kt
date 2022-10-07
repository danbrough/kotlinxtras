package org.danbrough.kotlinxtras.sonatype


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import java.net.URI


open class SonatypeExtension(private val project: Project) {
  companion object {
    const val SONATYPE_TASK_GROUP = "sonatype"
    const val REPOSITORY_ID = "sonatypeRepositoryId"
    const val DESCRIPTION = "sonatypeDescription"
  }

  var configurePublishing: PublishingExtension.(project:Project)->Unit = {}

  val sonatypeUrlBase: String = "https://s01.oss.sonatype.org"
  val sonatypeProfileId: String by project.properties
  val sonatypeRepositoryId: String by project.properties
  val sonatypeUsername: String by project.properties
  val sonatypePassword: String by project.properties
  var signPublications:Boolean = project.properties.containsKey("signPublications")
  private val sonatypeSnapshot: String by project.properties

  val publishingURL: String
    get() = if (sonatypeSnapshot.toBoolean())
      "$sonatypeUrlBase/content/repositories/snapshots/"
    else if (sonatypeRepositoryId.isNotBlank())
      "$sonatypeUrlBase/service/local/staging/deployByRepositoryId/$sonatypeRepositoryId"
    else
      "$sonatypeUrlBase/service/local/staging/deploy/maven2/"


  override fun toString() =
    "SonatypeExtension[urlBase=$sonatypeUrlBase,stagingProfileId=$sonatypeProfileId,sonatypeUsername=$sonatypeUsername]"

}



fun Project.declareRepositories(extn: SonatypeExtension){

  afterEvaluate {

    extensions.findByType<PublishingExtension>()?.apply {
      if (extn.signPublications) {
        it.apply<SigningPlugin>()
        it.extensions.getByType<SigningExtension>().also {signingExtension->
          publications.all {publication->
            signingExtension.sign(publication)
          }
        }
      }

      repositories { handler ->
        handler.maven { repo ->
          repo.name = "SonaType"
          repo.url = URI(extn.publishingURL)
          repo.credentials { creds->
            creds.username = extn.sonatypeUsername
            creds.password = extn.sonatypePassword
          }
        }
      }

      extn.configurePublishing.invoke(this,it)


    }
    it.childProjects.forEach {entry->
      entry.value.declareRepositories(extn)
    }
  }

}

class SonatypePlugin : Plugin<Project> {
  override fun apply(project: Project) {

    project.logger.info("sonatype configuring $project")
    val extn = project.extensions.create("sonatype", SonatypeExtension::class.java, project)

    project.createOpenRepoTask(extn)
    project.createCloseRepoTask(extn)
    project.declareRepositories(extn)

    project.afterEvaluate {
      project.extensions.findByType<PublishingExtension>()?.apply {
        repositories {
          it.maven { repo ->
            repo.name = "SonaType"
            repo.setUrl(extn.publishingURL)
            repo.credentials { creds ->
              creds.username = extn.sonatypeUsername
              creds.password = extn.sonatypePassword
            }
          }
        }
      }
    }
  }
}




