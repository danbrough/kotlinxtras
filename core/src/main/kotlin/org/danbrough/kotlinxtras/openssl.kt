@file:Suppress("unused")

package org.danbrough.kotlinxtras


import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

const val XTRAS_OPENSSL_EXTN_NAME = "openssl"


fun Project.enableOpenssl(extnName: String = XTRAS_OPENSSL_EXTN_NAME,config:LibraryExtension.()->Unit = {}):LibraryExtension =
  registerLibraryExtension(extnName) {

    publishingGroup = CORE_PUBLISHING_PACKAGE
    version = "1_1_1s"

    git("https://github.com/danbrough/openssl.git", "02e6fd7998830218909cbc484ca054c5916fdc59")

    configure { target ->
      outputs.file(workingDir.resolve("Makefile"))
      val args = mutableListOf(
        "./Configure",
        target.opensslPlatform,
        "no-tests",
        "threads",
        "--prefix=${buildDir(target)}"
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




val KonanTarget.opensslPlatform: String
  get() = when (this) {
    KonanTarget.LINUX_X64 -> "linux-x86_64"
    KonanTarget.LINUX_ARM64 -> "linux-aarch64"
    KonanTarget.LINUX_ARM32_HFP -> "linux-armv4"
    KonanTarget.LINUX_MIPS32 -> TODO()
    KonanTarget.LINUX_MIPSEL32 -> TODO()
    KonanTarget.ANDROID_ARM32 -> "android-arm"
    KonanTarget.ANDROID_ARM64 -> "android-arm64"
    KonanTarget.ANDROID_X86 -> "android-x86"
    KonanTarget.ANDROID_X64 -> "android-x86_64"
    KonanTarget.MINGW_X64 -> "mingw64"
    KonanTarget.MINGW_X86 -> "mingw"

    KonanTarget.MACOS_X64 -> "darwin64-x86_64-cc"
    KonanTarget.MACOS_ARM64 -> "darwin64-arm64-cc"
    KonanTarget.IOS_ARM32 -> "ios-cross"
    KonanTarget.IOS_ARM64 -> "ios64-cross" //ios-cross ios-xcrun
    KonanTarget.IOS_SIMULATOR_ARM64 -> "iossimulator-xcrun"
    KonanTarget.IOS_X64 -> "ios64-cross"

    KonanTarget.TVOS_ARM64 -> TODO()
    KonanTarget.TVOS_SIMULATOR_ARM64 -> TODO()
    KonanTarget.TVOS_X64 -> TODO()
    KonanTarget.WASM32 -> TODO()
    KonanTarget.WATCHOS_ARM32 -> TODO()
    KonanTarget.WATCHOS_ARM64 -> TODO()
    KonanTarget.WATCHOS_SIMULATOR_ARM64 -> TODO()
    KonanTarget.WATCHOS_X64 -> TODO()
    KonanTarget.WATCHOS_X86 -> TODO()
    is KonanTarget.ZEPHYR -> TODO()
  }

