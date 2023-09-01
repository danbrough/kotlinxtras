package org.danbrough.kotlinxtras.curl

import org.danbrough.kotlinxtras.XtrasDSLMarker
import org.danbrough.kotlinxtras.XtrasPlugin
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.danbrough.kotlinxtras.library.xtrasCreateLibrary
import org.danbrough.kotlinxtras.library.xtrasRegisterSourceTask
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.source.gitSource
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

object Curl {
  const val extensionName = "curl"
  const val sourceURL = "https://github.com/curl/curl.git"
}


class CurlPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.log("CURL PLUGIN: apply()")
  }
}

const val CURL_VERSION = "8.2.0"
const val CURL_COMMIT = "curl-8_2_0"

@XtrasDSLMarker
fun Project.xtrasCurl(
  name: String = Curl.extensionName,
  curlVersion: String = properties.getOrDefault("$name.version", CURL_VERSION).toString(),
  curlCommit: String = properties.getOrDefault("$name.commit", CURL_COMMIT).toString(),
  configure: XtrasLibrary.() -> Unit = {},
) = xtrasCreateLibrary(name, curlVersion) {
  gitSource(Curl.sourceURL, curlCommit)
  configure()

  supportedTargets.forEach { target ->
    val prepareSourceTask = prepareSourceTaskName(target)
    xtrasRegisterSourceTask(prepareSourceTask, target) {
      outputs.file("configure")
      dependsOn(extractSourceTaskName(target))
      commandLine(buildEnv.binaries.autoreconf, "-fi")
    }

    val configureTask = configureTaskName(target)
    xtrasRegisterSourceTask(configureTask, target) {
      dependsOn(prepareSourceTask)
      outputs.file(workingDir.resolve("Makefile"))

      val configureOptions = mutableListOf(
        "./configure",
        "--host=${target.hostTriplet}",
        //TODO "--with-wolfssl=${ssl.libsDir(target)}",
        //"--with-ca-path=/etc/ssl/certs:/etc/security/cacerts:/etc/ca-certificates",
        "--prefix=${buildDir(target)}"
      )
      commandLine(configureOptions)
    }
  }


}


