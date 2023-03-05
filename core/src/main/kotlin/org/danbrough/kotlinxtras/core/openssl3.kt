@file:Suppress("unused")

package org.danbrough.kotlinxtras.core


import org.danbrough.kotlinxtras.binaries.*
import org.danbrough.kotlinxtras.hostTriplet
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family

const val OPENSSL3_EXTN_NAME = "openssl3"

const val OPENSSL3_VERSION = "3.0.8-danbrough"
const val OPENSSL3_GIT_COMMIT = "e4e4c3b72620cf8ef35c275271415bfc675ffaa3"

fun Project.enableOpenssl3(
  extnName: String = OPENSSL3_EXTN_NAME,
  versionName: String = OPENSSL3_VERSION,
  commit: String = OPENSSL3_GIT_COMMIT,
  gitURL: String = OPENSSL_GIT_URL,
  config: LibraryExtension.() -> Unit = {}
): LibraryExtension =
  extensions.findByName(extnName) as? LibraryExtension ?: registerLibraryExtension(extnName) {

    publishingGroup = CORE_PUBLISHING_PACKAGE

    version = versionName

    git(gitURL, commit)

    configure { target ->
      outputs.file(workingDir.resolve("Makefile"))
      val args = mutableListOf(
        "./Configure", target.opensslPlatform, "no-tests", "threads", "--prefix=${buildDir(target)}"
      )
      if (target.family == Family.ANDROID) args += "-D__ANDROID_API__=21"
      else if (target.family == Family.MINGW) args += "--cross-compile-prefix=${target.hostTriplet}-"

      commandLine(args)
    }


    build {
      commandLine(binaries.makeBinary, "install_sw")
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

    config()
  }

