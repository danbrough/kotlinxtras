@file:Suppress("unused")

package org.danbrough.kotlinxtras


import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget

const val XTRAS_CURL_EXTN_NAME = "curl"

fun Project.enableCurl(name: String = XTRAS_CURL_EXTN_NAME,config:LibraryExtension.()->Unit = {}):LibraryExtension {
  val openSSL =
    (extensions.findByName(XTRAS_OPENSSL_EXTN_NAME) ?: enableOpenssl()) as LibraryExtension

  return registerLibraryExtension(name) {
    publishingGroup = CORE_PUBLISHING_PACKAGE

    version = "7_86_0"

    git("https://github.com/curl/curl.git", "cd95ee9f771361acf241629d2fe5507e308082a2")

    val autoConfTaskName: KonanTarget.() -> String =
      { "xtrasAutoconf${libName.capitalized()}${platformName.capitalized()}" }

    configureTarget { target ->
      project.tasks.create(target.autoConfTaskName(), Exec::class.java) {
        onlyIf { !isPackageBuilt(target) }
        dependsOn(extractSourcesTaskName(target))
        workingDir(sourcesDir(target))
        outputs.file(workingDir.resolve("configure"))
        commandLine(binaries.autoreconfBinary, "-fi")
      }
    }


    configure { target ->
      dependsOn(target.autoConfTaskName())

      val provideOpenSSLTaskName = provideBinariesTaskName(target, "openssl")

      val provideOpenSSLTask = project.tasks.getByName(provideOpenSSLTaskName)
      dependsOn(provideOpenSSLTask)

      //println("CurlPlugin: provideOpenSSLTask: $provideOpenSSLTask outputs: ${provideOpenSSLTask.outputs.files.files}")

      outputs.file(workingDir.resolve("Makefile"))

      commandLine(
        "./configure",
        "--host=${target.hostTriplet}",
        "--with-ssl=${openSSL.libsDir(target)}",
        "--with-ca-path=/etc/ssl/certs:/etc/security/cacerts:/etc/ca-certificates",
        "--prefix=${buildDir(target)}"
      )
    }

    build {
      commandLine(binaries.makeBinary, "install")
    }

    cinterops {
      headers = """
          headers = curl/curl.h
          linkerOpts =  -lz -lssl -lcrypto -lcurl
          #staticLibraries.linux = libcurl.a
          #staticLibraries.android = libcurl.a
          
          """.trimIndent()
    }

    config()
  }
}



