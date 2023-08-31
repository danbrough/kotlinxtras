@file:Suppress("PropertyName")

import org.danbrough.kotlinxtras.XtrasDSLMarker
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.danbrough.kotlinxtras.library.xtrasCreateLibrary
import org.danbrough.kotlinxtras.library.xtrasRegisterSourceTask
import org.danbrough.kotlinxtras.source.gitSource

plugins {
  //alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras)
  `java-gradle-plugin`
}

object Curl {
  const val extensionName = "curl"
  const val sourceURL = "https://github.com/curl/curl.git"
}


@XtrasDSLMarker
fun Project.xtrasCurl(
  name: String = Curl.extensionName,
  configure: XtrasLibrary.() -> Unit = {},
  curlVersion: String = properties.getOrDefault("curl.version", "8.2.1").toString(),
  curlCommit: String = properties.getOrDefault("curl.commit", "curl-8_2_1").toString(),
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
    }
  }
}



