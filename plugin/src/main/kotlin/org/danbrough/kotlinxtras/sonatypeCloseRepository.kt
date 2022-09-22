package org.danbrough.kotlinxtras

import java.io.InputStreamReader
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
    InputStreamReader(inputStream).use {
      it.readText().also {
        println("RESPONSE: $it")
      }
    }
  }
}