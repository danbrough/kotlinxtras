@file:Suppress("unused")

package org.danbrough.kotlinxtras


import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family

const val XTRAS_OPENSSL_EXTN_NAME = "xtrasOpenssl"

open class OpenSSLBinaryExtension(project: Project) : LibraryExtension(project, "openssl")


class OpenSSLPlugin : Plugin<Project> {
  override fun apply(project: Project) {

    project.registerLibraryExtension(XTRAS_OPENSSL_EXTN_NAME, OpenSSLBinaryExtension::class.java) {

      version = "1_1_1s"

      git("https://github.com/danbrough/openssl.git", "02e6fd7998830218909cbc484ca054c5916fdc59")

      configure { target ->
        outputs.file(workingDir.resolve("Makefile"))
        val args = mutableListOf(
          "./Configure",
          target.opensslPlatform,
          "no-tests",
          "threads",
          "--prefix=${prefixDir(target)}"
        )
        if (target.family == Family.ANDROID) args += "-D__ANDROID_API__=21"
        else if (target.family == Family.MINGW) args += "--cross-compile-prefix=${target.hostTriplet}-"

        commandLine(args)
      }


      build { target ->
        commandLine(binaries.makeBinary, "install_sw")
        outputs.dir(prefixDir(target))
      }

      cinterops {
        headers = """
          #staticLibraries =  libcrypto.a libssl.a
          headers = openssl/ssl.h openssl/err.h openssl/bio.h openssl/evp.h
          linkerOpts.linux = -ldl -lc -lm -lssl -lcrypto
          linkerOpts.android = -ldl -lc -lm -lssl -lcrypto
          linkerOpts.macos = -ldl -lc -lm -lssl -lcrypto
          linkerOpts.mingw = -lm -lssl -lcrypto
          compilerOpts.android = -D__ANDROID_API__=21
          compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
          #compilerOpts = -static
          
          """.trimIndent()
      }
    }
  }
}




