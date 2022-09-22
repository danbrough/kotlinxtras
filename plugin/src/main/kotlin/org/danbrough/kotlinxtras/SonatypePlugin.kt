package org.danbrough.kotlinxtras


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.findByType


/*
      //stagingProfileId.set("98edb69227dc82")
      //nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      //nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/orgdanbrough-1171/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
 */





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

    println("sonatype configuring $project")
    val extn = project.extensions.create("sonatype", SonatypeExtension::class.java)
    extn.init(project)

    println("sona publishing url: ${extn.publishingUrl}")

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


internal  fun Project.createOpenRepoTask(extn:SonatypeExtension){
  project.tasks.create("sonatypeOpenRepository") {task->
    task.description =
      "Open a new sonatype repository and store the repository id in gradle.properties"
    task.group = SONATYPE_TASK_GROUP
    task.doLast {_->
      if (extn.stagingProfileId.isBlank()) throw Error("sonatype.stagingProfileId not set")
      val description =
        project.properties[SonatypeProperties.DESCRIPTION]?.toString() ?: ""

      val response = sonatypeOpenRepository(
        extn.stagingProfileId,
        description,
        extn.username,
        extn.password,
        extn.sonatypeUrlBase
      )
      project.logger.info("Received response: $response")

      project.rootProject.file("gradle.properties").readLines().also { lines ->
        var wroteRepoId= false
        var writeRepoDescription = false
        project.rootProject.file("gradle.properties").printWriter().use {output->
          lines.forEach {
            if (it.startsWith(SonatypeProperties.REPOSITORY_ID)){
              wroteRepoId = true
              output.println("${SonatypeProperties.REPOSITORY_ID}=${response.repositoryId}")
            } else if (it.startsWith(SonatypeProperties.DESCRIPTION)){
              writeRepoDescription = true
              output.println("${SonatypeProperties.DESCRIPTION}=${response.description}")
            } else {
              output.println(it)
            }
          }
          if (!wroteRepoId){
            output.println("${SonatypeProperties.REPOSITORY_ID}=${response.repositoryId}")
          }
          if (!writeRepoDescription){
            output.println("${SonatypeProperties.DESCRIPTION}=${response.description}")
          }
        }
      }
    }
  }
}


internal fun Project.createCloseRepoTask(extn:SonatypeExtension){
  project.tasks.create("sonatypeCloseRepository") {task->
    task.description =
      "Closes the sonatype repository as specified by the ${SonatypeProperties.REPOSITORY_ID} gradle property"
    task.group = SONATYPE_TASK_GROUP
    task.doLast {_->
      if (extn.stagingProfileId.isBlank()) throw Error("sonatype.stagingProfileId not set")
      val description =
        project.properties[SonatypeProperties.DESCRIPTION]?.toString() ?: ""

      val response = sonatypeCloseRepository(
        extn.stagingProfileId,
        extn.repoId,
        description,
        extn.username,
        extn.password,
        extn.sonatypeUrlBase
      )
      project.logger.info("Received response: $response")

//      project.rootProject.file("gradle.properties").readLines().also { lines ->
//        var wroteRepoId= false
//        var writeRepoDescription = false
//        project.rootProject.file("gradle.properties").printWriter().use {output->
//          lines.forEach {
//            if (it.startsWith(SonatypeProperties.REPOSITORY_ID)){
//              wroteRepoId = true
//              output.println("${SonatypeProperties.REPOSITORY_ID}=${response.repositoryId}")
//            } else if (it.startsWith(SonatypeProperties.REPOSITORY_DESCRIPTION)){
//              writeRepoDescription = true
//              output.println("${SonatypeProperties.REPOSITORY_DESCRIPTION}=${response.description}")
//            } else {
//              output.println(it)
//            }
//          }
//          if (!wroteRepoId){
//            output.println("${SonatypeProperties.REPOSITORY_ID}=${response.repositoryId}")
//          }
//          if (!writeRepoDescription){
//            output.println("${SonatypeProperties.REPOSITORY_DESCRIPTION}=${response.description}")
//          }
//        }
//      }
    }
  }
}
