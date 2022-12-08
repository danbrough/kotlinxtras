package org.danbrough.kotlinxtras.sonatype


import org.danbrough.kotlinxtras.xtrasMavenDir
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import java.io.File

class SonatypePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extn = project.extensions.create("sonatype", SonatypeExtension::class.java, project)

    val publishingPluginId = "org.gradle.maven-publish"
    project.plugins.findPlugin(publishingPluginId) ?: project.pluginManager.apply(publishingPluginId)

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
    const val REPOSITORY_ID = "sonatypeRepoId"
    const val PROFILE_ID = "sonatypeProfileId"
    const val DESCRIPTION = "sonatypeDescription"
    const val USERNAME = "sonatypeUsername"
    const val PASSWORD = "sonatypePassword"
    const val PUBLISH_DOCS = "publishDocs"
    const val SIGN_PUBLICATIONS = "signPublications"
  }

  internal var configurePublishing: PublishingExtension.(project: Project) -> Unit = {}

  fun configurePublishing(configure: PublishingExtension.(project: Project) -> Unit) {
    configurePublishing = configure
  }

  var sonatypeUrlBase: String = "https://s01.oss.sonatype.org"
  var sonatypeProfileId: String? = null
  var sonatypeRepoId: String? = null
  var sonatypeUsername: String? = null
  var sonatypePassword: String? = null
  var publishDocs: Boolean = false
  var signPublications: Boolean = false

  var localRepoEnabled: Boolean = true
  var localRepoName: String = "xtras"
  var localRepoLocation: File = project.xtrasMavenDir

  var sonatypeSnapshot: Boolean  = false

  //lateinit var publishingURL:String
/*
  val publishingURL: String
    get() = (if (sonatypeSnapshot)
      "$sonatypeUrlBase/content/repositories/snapshots/"
    else if (sonatypeRepoId.isNotBlank())
      "$sonatypeUrlBase/service/local/staging/deployByRepositoryId/$sonatypeRepoId"
    else
      "$sonatypeUrlBase/service/local/staging/deploy/maven2/").also {
        println("SonatypeExtension::publishingURL $it repoID is: $sonatypeRepoId")
    }
*/

  override fun toString() =
    "SonatypeExtension[urlBase=$sonatypeUrlBase,stagingProfileId=$sonatypeProfileId,sonatypeUsername=$sonatypeUsername]"
}






