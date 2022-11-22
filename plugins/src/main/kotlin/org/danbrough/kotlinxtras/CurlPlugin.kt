@file:Suppress("unused")

package org.danbrough.kotlinxtras


import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized


class CurlPlugin : Plugin<Project> {


  override fun apply(project: Project) {

    project.extensions.findByName("openssl") ?: throw Error("Openssl plugin is required.")

    project.registerBinariesExtension("curl"){

      git("https://github.com/curl/curl.git", "cd95ee9f771361acf241629d2fe5507e308082a2")


      configureTarget {target->
        project.tasks.create("autoconf${target.platformName.capitalized()}", Exec::class.java) {
          dependsOn(extractSourcesTaskName(target))
          val sourcesDir = sourcesDir(target)
          workingDir(sourcesDir)
          outputs.file(sourcesDir.resolve("configure"))
          commandLine("autoreconf", "-fi")
        }
      }


      configure { target ->
        val sourcesDir = sourcesDir(target)
        dependsOn("autoconf${target.platformName.capitalized()}")
        val buildOpenSSLTaskName = buildSourcesTaskName(target,"openssl")
        val buildOpensslTask = project.tasks.getByName(buildOpenSSLTaskName)
        val opensslDir = buildOpensslTask.outputs.files.files.first()
        dependsOn(buildOpenSSLTaskName)
        outputs.file(sourcesDir.resolve("Makefile"))

        val args = mutableListOf(
          "./configure",
          "--host=${target.hostTriplet}",
          "--with-ssl=$opensslDir",
          "--with-ca-path=/etc/ssl/certs:/etc/security/cacerts:/etc/ca-certificates",
          "--prefix=${prefixDir(target)}"
        )

        commandLine(args)
      }


      build {
        commandLine("make", "install")
      }
    }
  }
}



