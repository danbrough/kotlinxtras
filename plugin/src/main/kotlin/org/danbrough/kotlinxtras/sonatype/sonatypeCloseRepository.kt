package org.danbrough.kotlinxtras.sonatype

import org.gradle.api.Project
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.*

fun sonatypeCloseRepository(
  stagingProfileId: String,
  repoId:String,
  description: String,
  username: String,
  password: String,
  urlBase: String = "https://s01.oss.sonatype.org"
) {
  println("sonatypeOpenRepository: ")
  val url = "$urlBase/service/local/staging/profiles/$stagingProfileId/finish"
  URL(url).openConnection().apply {
    this as HttpURLConnection
    requestMethod = "POST"
    doOutput = true
    addRequestProperty("Content-Type", "application/xml")
    addRequestProperty(
      "Authorization",
      "Basic: ${
        Base64.getEncoder()
          .encodeToString("$username:$password".toByteArray(Charset.defaultCharset()))
      }"
    )
    PrintWriter(outputStream).use { output ->
      output.write(
        """
          <promoteRequest>
    <data>
        <stagedRepositoryId>$repoId</stagedRepositoryId>
        <description>$description</description>
    </data>
</promoteRequest>""".trimIndent()
      )
    }

    println("RESPONSE: $responseCode : $responseMessage")
    if (responseCode != HttpURLConnection.HTTP_CREATED)
      throw Error("Response code: $responseCode $responseMessage")

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

      sonatypeCloseRepository(
        extn.stagingProfileId,
        extn.repoId,
        description,
        extn.username,
        extn.password,
        extn.sonatypeUrlBase
      )
    }
  }
}
