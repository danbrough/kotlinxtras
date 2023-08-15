package org.danbrough.kotlinxtras.core

import org.danbrough.kotlinxtras.binaries.LibraryExtension
import org.danbrough.kotlinxtras.binaries.git
import org.danbrough.kotlinxtras.binaries.registerLibraryExtension
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.platformName
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

const val WOLFSSL_EXTN_NAME = "wolfSSL"

const val WOLFSSL_VERSION = "5.6.3"
const val WOLFSSL_SOURCE = "https://github.com/wolfSSL/wolfssl.git"
const val WOLFSSL_COMMIT = "3b3c175af0e993ffaae251871421e206cc41963f"

fun Project.enableWolfSSL(
  extnName: String = WOLFSSL_EXTN_NAME,
  versionName: String = WOLFSSL_VERSION,
  commit: String = WOLFSSL_COMMIT,
  gitURL: String = WOLFSSL_SOURCE,
  config: LibraryExtension.() -> Unit = {}
): LibraryExtension =
  extensions.findByName(extnName) as? LibraryExtension ?: registerLibraryExtension(extnName) {
    binaries.androidNdkDir = File("/mnt/files/sdk/android/ndk/25.0.8775105/")

    val autogenTaskName: KonanTarget.() -> String =
      { "xtrasAutogen${libName.capitalized()}${platformName.capitalized()}" }

    publishingGroup = CORE_PUBLISHING_PACKAGE

    version = versionName

    git(gitURL, commit)

    configureTarget { target ->
      project.tasks.create(target.autogenTaskName(), Exec::class.java) {
        dependsOn(extractSourcesTaskName(target))
        workingDir(sourcesDir(target))
        outputs.file(workingDir.resolve("configure"))
        commandLine("./autogen.sh")

        environment(
          "PATH",
          "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
        )
      }
    }

    configure { target ->
      outputs.file(workingDir.resolve("Makefile"))
      dependsOn(target.autogenTaskName())

      val configureOptions = mutableListOf(
        "./configure",
        "--enable-jni", "--enable-openssh", "--enable-libssh2", "--enable-ssh",
        "--target=${target.hostTriplet}",
        "--prefix=${buildDir(target)}"
      )

      project.log("configuring with $configureOptions CC is ${environment["CC"]} CFLAGS: ${environment["CFLAGS"]}")

      commandLine(configureOptions)
    }

    build {
      commandLine(binaries.makeBinary, "install")
    }
    cinterops {
      headers = """
          #staticLibraries =  libcrypto.a libssl.a
          headers =  ssl.h openssl/ssl.h openssl/err.h openssl/bio.h openssl/evp.h
          linkerOpts.linux = -ldl -lc -lm -lwolfssl 
          linkerOpts.android = -ldl -lc -lm -lwolfssl
          linkerOpts.macos = -ldl -lc -lm -lwolfssl 
          linkerOpts.mingw = -lm -lwolfssl 
          compilerOpts.android = -D__ANDROID_API__=21
          #compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
          #compilerOpts = -static
          
          """.trimIndent()
    }

    config()
  }