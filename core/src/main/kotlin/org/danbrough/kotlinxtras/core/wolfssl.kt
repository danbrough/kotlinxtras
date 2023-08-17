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
//    binaries.androidNdkDir = File("/mnt/files/sdk/android/ndk/25.0.8775105/")

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
      //binaries.androidNdkDir = File("/mnt/files/sdk/android/ndk/25.0.8775105/")

      outputs.file(workingDir.resolve("Makefile"))
      dependsOn(target.autogenTaskName())

      /*  val configureOptions = mutableListOf(
          "./configure",
          "--host=${target.hostTriplet}",
          "--enable-openssh",
          "--enable-libssh2",
          "--enable-ssh",
          "--disable-examples",
          "--prefix=${buildDir(target)}"
        )
  */


      val configureOptions = mutableListOf(
        "./configure",
        "--host=${target.hostTriplet}",
        "--prefix=${buildDir(target)}",
//      "--disable-fasthugemath",
//      "--disable-bump",
//      "--enable-opensslextra",
//      "--enable-fortress",
//      "--disable-debug",
//      "--disable-ntru",
//      "--disable-examples",
//      "--enable-distro",
//      "--enable-reproducible-build",
        "--enable-curve25519",
        "--enable-ed25519",
        "--enable-curve448",
        "--enable-ed448",
        "--enable-sha512",
        "--with-max-rsa-bits=8192",


//  --enable-certreq        Enable cert request generation (default: disabled)
        "--enable-certext",//        Enable cert request extensions (default: disabled)
//  --enable-certgencache   Enable decoded cert caching (default: disabled)
        //--enable-altcertchains  Enable using alternative certificate chains, only
        //   require leaf certificate to validate to trust root
        //--enable-testcert       Enable Test Cert (default: disabled)
        "--enable-certservice",
        "--enable-altcertchains",
//      "--enable-writedup",

        "--enable-opensslextra",
        "--enable-openssh",
        "--enable-libssh2",
        "--enable-keygen", "--enable-certgen",
        "--enable-ssh", "--enable-wolfssh",
        "--disable-examples", "--enable-postauth",

        )
      if (target != KonanTarget.MINGW_X64)
        configureOptions.add("--enable-jni")
      project.log("configuring with $configureOptions CC is ${environment["CC"]} CFLAGS: ${environment["CFLAGS"]}")

      when (target) {
        KonanTarget.ANDROID_ARM32, KonanTarget.ANDROID_ARM64, KonanTarget.ANDROID_X64, KonanTarget.ANDROID_X86 -> {
          environment(
            "PATH",
            "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
          )
        }

        else -> {}
      }

      commandLine(configureOptions)
    }

    build { target ->
      when (target) {
        KonanTarget.ANDROID_ARM32, KonanTarget.ANDROID_ARM64, KonanTarget.ANDROID_X64, KonanTarget.ANDROID_X86 -> {
          environment(
            "PATH",
            "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
          )
        }

        else -> {}
      }

      commandLine(binaries.makeBinary, "install")
    }
    cinterops {
      headers = """
          #staticLibraries =  libcrypto.a libssl.a
          headers =  wolfssl/ssl.h wolfssl/openssl/ssl.h wolfssl/openssl/err.h wolfssl/openssl/bio.h wolfssl/openssl/evp.h
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