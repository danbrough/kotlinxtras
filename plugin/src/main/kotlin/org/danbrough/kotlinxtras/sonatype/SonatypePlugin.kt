package org.danbrough.kotlinxtras.sonatype


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import java.io.File

class SonatypePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    println("sonatype configuring ${project.name}")
    val extn = project.extensions.create("sonatype", SonatypeExtension::class.java, project)

    project.afterEvaluate {
      project.createOpenRepoTask(extn)
      project.createCloseRepoTask(extn)
      project.configurePublishing(extn)
    }
  }
}

open class SonatypeExtension( project: Project) {
  companion object {
    const val SONATYPE_TASK_GROUP = "sonatype"
    const val REPOSITORY_ID = "sonatypeRepositoryId"
    const val DESCRIPTION = "description"
  }

  internal var configurePublishing: PublishingExtension.(project: Project) -> Unit = {}

  fun configurePublishing(configure:PublishingExtension.(project: Project) -> Unit){
    configurePublishing = configure
  }

  val sonatypeUrlBase: String = "https://s01.oss.sonatype.org"
  val sonatypeProfileId: String by project.properties
  val sonatypeRepositoryId: String by project.properties
  val sonatypeUsername: String by project.properties
  val sonatypePassword: String by project.properties
  var publishDocs: Boolean = project.properties.containsKey("publishDocs")
  var signPublications: Boolean = project.properties.containsKey("signPublications")

  var localRepoEnabled:Boolean = true
  var localRepoName:String = "m2"
  var localRepoLocation : File =  project.rootProject.buildDir.resolve(localRepoName)

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


