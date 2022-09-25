package org.danbrough.kotlinxtras.sonatype


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.findByType


object SonatypeProperties {
  const val USERNAME = "sonatypeUsername"
  const val PASSWORD = "sonatypePassword"
  const val PROFILE_ID = "sonatypeProfileId"
  const val REPOSITORY_ID = "sonatypeRepositoryId"
  const val SNAPSHOT = "sonatypeSnapshot"
  const val DESCRIPTION = "sonatypeDescription"
}

const val SONATYPE_TASK_GROUP = "sonatype"


open class SonatypeExtension {
  var sonatypeUrlBase: String = "https://s01.oss.sonatype.org"

  var stagingProfileId: String = ""
  var repoId: String = ""
  var username: String = ""
  var password: String = ""
  var publishingUrl: String = ""
  var localMavenRepoPath: String = ""

  override fun toString() = "SonatypeExtension[urlBase=$sonatypeUrlBase,stagingProfileId=$stagingProfileId,username=$username]"

  internal fun init(project: Project) {
    if (stagingProfileId.isBlank()) {
      project.properties[SonatypeProperties.PROFILE_ID]?.toString()?.also {
        stagingProfileId = it
      }
    }

    if (username.isBlank()) {
      project.properties[SonatypeProperties.USERNAME]?.toString()?.also {
        username = it
      }
    }

    if (password.isBlank()) {
      project.properties[SonatypeProperties.PASSWORD]?.toString()?.also {
        password = it
      }
    }

    if (repoId.isBlank()) repoId =
      project.properties[SonatypeProperties.REPOSITORY_ID]?.toString() ?: ""


    val publishSnapshot =
      project.properties[SonatypeProperties.SNAPSHOT]?.toString()?.toBoolean() ?: false


    if (publishingUrl.isBlank()) {
      publishingUrl = if (publishSnapshot)
        "$sonatypeUrlBase/content/repositories/snapshots/"
      else if (repoId.isNotBlank())
        "$sonatypeUrlBase/service/local/staging/deployByRepositoryId/$repoId/"
      else
        "$sonatypeUrlBase/service/local/staging/deploy/maven2/"
    }
  }
}



class SonatypePlugin : Plugin<Project> {
  override fun apply(project: Project) {

    project.logger.info("sonatype configuring $project")
    val extn = project.extensions.create("sonatype", SonatypeExtension::class.java)
    extn.init(project)

    project.logger.info("sona publishing url: ${extn.publishingUrl}")

    project.createOpenRepoTask(extn)
    project.createCloseRepoTask(extn)

    project.afterEvaluate {
      project.extensions.findByType<PublishingExtension>()?.apply {

        if (extn.localMavenRepoPath.isNotBlank())
          repositories {
            it.maven { repo ->
              repo.name = "M2"
              repo.setUrl(extn.localMavenRepoPath)
            }
          }

        repositories {
          it.maven { repo ->
            repo.name = "SonaType"
            repo.setUrl(extn.publishingUrl)
            repo.credentials { creds ->
              creds.username = extn.username
              creds.password = extn.password
            }
          }
        }
      }
    }
  }
}




