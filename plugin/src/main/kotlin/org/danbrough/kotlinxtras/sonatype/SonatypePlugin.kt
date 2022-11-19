package org.danbrough.kotlinxtras.sonatype


import org.danbrough.kotlinxtras.binaries.KOTLIN_XTRAS_DIR_NAME
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
//      project.extensions.findByType<PublishingExtension>()?.apply {
//        repositories {
//          maven {
//            name = "SonaType"
//            setUrl(extn.publishingURL)
//            credentials {
//              username = extn.sonatypeUsername
//              password = extn.sonatypePassword
//            }
//          }
//        }
//      }
    }
  }
}

open class SonatypeExtension( project: Project) {
  companion object {
    const val SONATYPE_TASK_GROUP = "sonatype"
    const val REPOSITORY_ID = "sonatypeRepositoryId"
    const val DESCRIPTION = "description"
  }

  var configurePublishing: PublishingExtension.(project: Project) -> Unit = {}

  val sonatypeUrlBase: String = "https://s01.oss.sonatype.org"
  val sonatypeProfileId: String by project.properties
  val sonatypeRepositoryId: String by project.properties
  val sonatypeUsername: String by project.properties
  val sonatypePassword: String by project.properties
  var signPublications: Boolean = project.properties.containsKey("signPublications")

  var dokkaDir:File = project.rootProject.buildDir.resolve(KOTLIN_XTRAS_DIR_NAME).resolve("dokka")

  var localRepoEnabled:Boolean = true
  var localRepoName:String = "M2"
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


