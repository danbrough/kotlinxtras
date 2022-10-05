package org.danbrough.kotlinxtras.sonatype

import org.danbrough.kotlinxtras.sonatype.SonatypeExtension.Companion.SONATYPE_TASK_GROUP
import org.gradle.api.Project
import org.w3c.dom.Element
import java.io.InputStream
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory


fun sonatypeOpenRepository(
  stagingProfileId: String,
  description: String,
  username: String,
  password: String,
  urlBase: String
): PromoteRequestResponse {
  println("sonatypeOpenRepository: ")
  val url = "$urlBase/service/local/staging/profiles/$stagingProfileId/start"
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
        """<promoteRequest>
    <data>
        <description>$description</description>
    </data>
</promoteRequest>""".trimIndent()
      )
    }

    if (responseCode == HttpURLConnection.HTTP_CREATED)
      return parsePromoteRequest(inputStream)

    throw Error("Failed: error: $responseCode: $responseCode: $responseMessage")
  }
}

data class PromoteRequestResponse(val repositoryId: String, val description: String)

fun parsePromoteRequest(input: InputStream): PromoteRequestResponse {
/*
      <promoteResponse>
      <data>
        <stagedRepositoryId>orgdanbrough-1185</stagedRepositoryId>
        <description></description>
      </data>
    </promoteResponse>

*/


  val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    .parse(input)
  return doc.getElementsByTagName("data").item(0).run {
    this as Element
    val repoID = getElementsByTagName("stagedRepositoryId").item(0).textContent
    val description = getElementsByTagName("description").item(0).textContent
    PromoteRequestResponse(repoID, description)
  }

}

internal  fun Project.createOpenRepoTask(extn:SonatypeExtension){
  project.tasks.create("sonatypeOpenRepository") {task->
    task.description =
      """
        Open a new sonatype repository and store the repository id in gradle.properties.
        Specify the repository description with -PsonatypeDescription="..".
      """.trimMargin()
    task.group = SONATYPE_TASK_GROUP
    task.doLast {_->
      if (extn.stagingProfileId.isBlank()) throw Error("sonatype.stagingProfileId not set")
      val description =
        project.properties[SonatypeExtension.DESCRIPTION]?.toString() ?: ""

      val response = sonatypeOpenRepository(
        extn.stagingProfileId,
        description,
        extn.sonatypeUsername,
        extn.sonatypePassword,
        extn.sonatypeUrlBase
      )
      project.logger.info("Received response: $response")

      project.rootProject.file("gradle.properties").readLines().also { lines ->
        var wroteRepoId= false
        var writeRepoDescription = false
        project.rootProject.file("gradle.properties").printWriter().use {output->
          lines.forEach {
            if (it.startsWith(SonatypeExtension.REPOSITORY_ID)){
              wroteRepoId = true
              output.println("${SonatypeExtension.REPOSITORY_ID}=${response.repositoryId}")
            } else if (it.startsWith(SonatypeExtension.DESCRIPTION)){
              writeRepoDescription = true
              output.println("${SonatypeExtension.DESCRIPTION}=${response.description}")
            } else {
              output.println(it)
            }
          }
          if (!wroteRepoId){
            output.println("${SonatypeExtension.REPOSITORY_ID}=${response.repositoryId}")
          }
          if (!writeRepoDescription){
            output.println("${SonatypeExtension.DESCRIPTION}=${response.description}")
          }
        }
      }
    }
  }
}
