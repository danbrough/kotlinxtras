package org.danbrough.kotlinxtras.sonatype


import org.danbrough.kotlinxtras.projectProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import java.io.File

class SonatypePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.logger.info("sonatype configuring ${project.name}")
    val extn = project.extensions.create("sonatype", SonatypeExtension::class.java, project)

    project.afterEvaluate {
      project.createOpenRepoTask(extn)
      project.createCloseRepoTask(extn)
      project.configurePublishing(extn)
    }
  }
}


open class SonatypeExtension(val project: Project) {
  companion object {
    const val SONATYPE_TASK_GROUP = "sonatype"
    const val REPOSITORY_ID = "sonatypeRepositoryId"
    const val DESCRIPTION = "description"
  }

  internal var configurePublishing: PublishingExtension.(project: Project) -> Unit = {}

  fun configurePublishing(configure: PublishingExtension.(project: Project) -> Unit) {
    configurePublishing = configure
  }


  var sonatypeUrlBase: String = "https://s01.oss.sonatype.org"
  var sonatypeProfileId: String = project.projectProperty("sonatypeProfileId")
  var sonatypeRepositoryId: String = project.projectProperty(REPOSITORY_ID)
  var sonatypeUsername: String = project.projectProperty("sonatypeUsername")
  var sonatypePassword: String = project.projectProperty("sonatypePassword")
  var publishDocs: Boolean = project.projectProperty("publishDocs",false)
  var signPublications: Boolean = project.projectProperty("signPublications",false)

  var localRepoEnabled: Boolean = true
  var localRepoName: String = "m2"
  var localRepoLocation: File = project.rootProject.buildDir.resolve(localRepoName)

  var sonatypeSnapshot: Boolean  = project.projectProperty("sonatypeSnapshot",false)

  val publishingURL: String
    get() = if (sonatypeSnapshot)
      "$sonatypeUrlBase/content/repositories/snapshots/"
    else if (sonatypeRepositoryId.isNotBlank())
      "$sonatypeUrlBase/service/local/staging/deployByRepositoryId/$sonatypeRepositoryId"
    else
      "$sonatypeUrlBase/service/local/staging/deploy/maven2/"

  override fun toString() =
    "SonatypeExtension[urlBase=$sonatypeUrlBase,stagingProfileId=$sonatypeProfileId,sonatypeUsername=$sonatypeUsername]"

}






