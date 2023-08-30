@file:Suppress("unused")

package org.danbrough.kotlinxtras.core


import org.danbrough.kotlinxtras.binaries.*
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.platformName
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

const val XTRAS_CURL_EXTN_NAME = "curl"

fun Project.enableCurl(
  ssl: LibraryExtension,
  extnName: String = XTRAS_CURL_EXTN_NAME,
  config: LibraryExtension.() -> Unit = {}
): LibraryExtension {

  return extensions.findByName(extnName) as? LibraryExtension
    ?: registerLibraryExtension(extnName) {
      publishingGroup = CORE_PUBLISHING_PACKAGE
      // binaries.androidNdkDir = File("/mnt/files/sdk/android/ndk/25.0.8775105/")


      version = "8.2.1"

      //git("https://github.com/curl/curl.git", "046209e561b7e9b5aab1aef7daebf29ee6e6e8c7")
      //git("https://github.com/curl/curl.git", "b16d1fa8ee567b52c09a0f89940b07d8491b881d")
      git("https://github.com/curl/curl.git", "50490c0679fcd0e50bb3a8fbf2d9244845652cf0")

      //binaries.androidNdkDir = File("/mnt/files/sdk/android/ndk/25.0.8775105/")


      val autoConfTaskName: KonanTarget.() -> String =
        { "xtrasAutoconf${libName.capitalized()}${platformName.capitalized()}" }

      configureTarget { target ->

        project.tasks.create(target.autoConfTaskName(), Exec::class.java) {
          dependsOn(extractSourcesTaskName(target))
          workingDir(sourcesDir(target))
          outputs.file(workingDir.resolve("configure"))
          commandLine(binaries.autoreconfBinary, "-fi")

          environment(
            "PATH",
            "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
          )
        }
      }


      configure { target ->
        dependsOn(project.tasks.getByName(ssl.extractArchiveTaskName(target)))

        dependsOn(target.autoConfTaskName())



        outputs.file(workingDir.resolve("Makefile"))

        val configureOptions = mutableListOf(
          "./configure",
          "--host=${target.hostTriplet}",
          "--with-wolfssl=${ssl.libsDir(target)}",
          //"--with-ca-path=/etc/ssl/certs:/etc/security/cacerts:/etc/ca-certificates",
          "--prefix=${buildDir(target)}"
        )
        environment(
          "PATH",
          "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
        )

        commandLine(configureOptions)
      }

      build {
        environment(
          "PATH",
          "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
        )
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



