@file:Suppress("unused")

package org.danbrough.kotlinxtras


import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget

const val XTRAS_CURL_EXTN_NAME = "xtrasCurl"

open class CurlBinaryExtension(project: Project) : LibraryExtension(project, "curl")
class CurlPlugin : Plugin<Project> {

  override fun apply(project: Project) {

/*    project.extensions.findByType(OpenSSLPlugin::class.java)
      ?: throw Error("org.danbrough.kotlinxtras.openssl plugin not present")*/

    project.registerLibraryExtension(XTRAS_CURL_EXTN_NAME, CurlBinaryExtension::class.java) {

      version = "7_86_0"

      git("https://github.com/curl/curl.git", "cd95ee9f771361acf241629d2fe5507e308082a2")

      val autoConfTaskName: KonanTarget.() -> String =
        { "xtrasAutoconf${libName.capitalized()}${platformName.capitalized()}" }

      configureTarget { target ->
        project.tasks.create(target.autoConfTaskName(), Exec::class.java) {
          dependsOn(extractSourcesTaskName(target))
          workingDir(sourcesDir(target))
          outputs.file(workingDir.resolve("configure"))
          commandLine(binaries.autoreconfBinary, "-fi")
        }
      }


      configure { target ->
        dependsOn(target.autoConfTaskName())

        val provideOpenSSLTaskName = provideBinariesTaskName(target, "openssl")
        dependsOn(provideOpenSSLTaskName)

        val provideOpenSSLTask = project.tasks.getByName(provideOpenSSLTaskName)
        println("CurlPlugin: provideOpenSSLTask: $provideOpenSSLTask outputs: ${provideOpenSSLTask.outputs.files.files}")
        //println("openssl working dir: ${provideOpenSSLTask.work}")
        val opensslDir = provideOpenSSLTask.outputs.files.files.first()

        outputs.file(workingDir.resolve("Makefile"))

        commandLine(
          "./configure",
          "--host=${target.hostTriplet}",
          "--with-ssl=$opensslDir",
          "--with-ca-path=/etc/ssl/certs:/etc/security/cacerts:/etc/ca-certificates",
          "--prefix=${prefixDir(target)}"
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
    }
  }
}



