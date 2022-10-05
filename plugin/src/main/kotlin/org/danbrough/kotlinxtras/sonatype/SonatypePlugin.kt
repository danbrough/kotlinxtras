package org.danbrough.kotlinxtras.sonatype


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.findByType


open class SonatypeExtension(private val project: Project) {
  companion object {
    const val SONATYPE_TASK_GROUP = "sonatype"

    const val USERNAME = "sonatypeUsername"
    const val PASSWORD = "sonatypePassword"
    const val PROFILE_ID = "sonatypeProfileId"
    const val REPOSITORY_ID = "sonatypeRepositoryId"
    const val SNAPSHOT = "sonatypeSnapshot"
    const val DESCRIPTION = "sonatypeDescription"
  }

  val sonatypeUrlBase: String = "https://s01.oss.sonatype.org"
  val stagingProfileId: String by project.properties
  val sonatypeRepositoryId: String by project.properties
  val sonatypeUsername: String by project.properties
  val sonatypePassword: String by project.properties
  private val sonatypeSnapshot: String by project.properties

  val publishingURL: String
    get() = if (sonatypeSnapshot.toBoolean())
      "$sonatypeUrlBase/content/repositories/snapshots/"
    else if (sonatypeRepositoryId.isNotBlank())
      "$sonatypeUrlBase/service/local/staging/deployByRepositoryId/$sonatypeRepositoryId"
    else
      "$sonatypeUrlBase/service/local/staging/deploy/maven2/"


  override fun toString() =
    "SonatypeExtension[urlBase=$sonatypeUrlBase,stagingProfileId=$stagingProfileId,sonatypeUsername=$sonatypeUsername]"

}


class SonatypePlugin : Plugin<Project> {
  override fun apply(project: Project) {

    project.logger.info("sonatype configuring $project")
    val extn = project.extensions.create("sonatype", SonatypeExtension::class.java, project)

    project.createOpenRepoTask(extn)
    project.createCloseRepoTask(extn)

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




