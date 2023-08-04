@file:Suppress("unused")

package org.danbrough.kotlinxtras.core


import org.danbrough.kotlinxtras.binaries.*
import org.danbrough.kotlinxtras.hostTriplet
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import java.io.File

const val OPENSSL3_EXTN_NAME = "openssl3"

//const val OPENSSL3_VERSION = "3.1.2-danbrough"
//const val OPENSSL3_GIT_COMMIT = "5accd111b0d4689a978f2bbccd976583f493efab"
const val OPENSSL3_VERSION = "3.1.2"
const val OPENSSL3_GIT_COMMIT = "17a2c5111864d8e016c5f2d29c40a3746b559e9d"
//
//const val OPENSSL3_VERSION = "3.0.8"
//const val OPENSSL3_GIT_COMMIT = "e4e4c3b72620cf8ef35c275271415bfc675ffaa3"

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
      binaries.androidNdkDir = File("/mnt/files/sdk/android/ndk/25.0.8775105/")

      outputs.file(workingDir.resolve("Makefile"))
      val args = mutableListOf(
        "./Configure", target.opensslPlatform, "no-tests", "threads", "--prefix=${buildDir(target)}"
      )
      if (target.family == Family.ANDROID) args += "-D__ANDROID_API__=21"
      else if (target.family == Family.MINGW) args += "--cross-compile-prefix=${target.hostTriplet}-"
      environment("CFLAGS", "  -Wno-macro-redefined ")

      environment(
        "PATH",
        "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
      )
      commandLine(args)
    }



    build { target ->
      environment(
        "PATH",
        "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
      )

      commandLine(binaries.makeBinary, "install_sw")
      doLast {
        val buildDir = buildDir(target)
        if (buildDir.resolve("lib64").exists()) {
          buildDir.resolve("lib64").renameTo(buildDir.resolve("lib"))
        }
      }
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

