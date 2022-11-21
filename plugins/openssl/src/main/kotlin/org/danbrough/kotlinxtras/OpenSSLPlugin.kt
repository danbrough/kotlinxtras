@file:Suppress("unused")

package org.danbrough.kotlinxtras



import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family



class OpenSSLPlugin : Plugin<Project> {
  override fun apply(project: Project) {


    project.registerBinariesExtension("openssl") {
      version = "1_1_1s"

      git("https://github.com/danbrough/openssl.git","02e6fd7998830218909cbc484ca054c5916fdc59")

      configure { target ->
        val sourcesDir = sourcesDir(target)
        outputs.file(sourcesDir.resolve("Makefile"))
        val args = mutableListOf("./Configure",target.opensslPlatform,"no-tests","threads","--prefix=${prefixDir(target)}")
        if (target.family == Family.ANDROID)
          args += "-D__ANDROID_API__=21"
        else if (target.family == Family.MINGW)
          args += "--cross-compile-prefix=${target.hostTriplet}-"

        commandLine(args)
      }


      build {target->
        commandLine("make","install_sw")
        outputs.dir(prefixDir(target))
      }
    }
  }
}




