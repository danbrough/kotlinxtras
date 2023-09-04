package org.danbrough.kotlinxtras.openssl

import org.danbrough.kotlinxtras.XtrasDSLMarker
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.danbrough.kotlinxtras.library.xtrasCreateLibrary
import org.danbrough.kotlinxtras.library.xtrasRegisterSourceTask
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.source.gitSource
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

const val OPENSSL_VERSION = "3.1.2"
const val OPENSSL_COMMIT = "openssl-3.1.2"

object OpenSSL {
  const val extensionName = "openSSL"
  const val sourceURL = "https://github.com/openssl/openssl.git"
}

class OpenSSLPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.log("OpenSSLLPlugin.apply()")
  }
}


@XtrasDSLMarker
fun Project.xtrasOpenSSL(
  name: String = OpenSSL.extensionName,
  version: String = properties.getOrDefault("openssl.version",OPENSSL_VERSION).toString(),
  commit: String = properties.getOrDefault("openssl.commit",  OPENSSL_COMMIT).toString(),
  configure: XtrasLibrary.() -> Unit = {},
) = xtrasCreateLibrary(name, version) {
  gitSource(OpenSSL.sourceURL, commit)

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

  configure()

  supportedTargets.forEach { target ->

    val configureTask = configureTaskName(target)
    xtrasRegisterSourceTask(configureTask, target) {
      dependsOn(libraryDeps.map { it.extractArchiveTaskName(target) })
      outputs.file(workingDir.resolve("Makefile"))
      val args = mutableListOf(
        "./Configure", target.opensslPlatform, "no-tests", "threads", "--prefix=${buildDir(target)}"
      )

      if (target.family == Family.ANDROID) args += "-D__ANDROID_API__=21"
      else if (target.family == Family.MINGW) args += "--cross-compile-prefix=${target.hostTriplet}-"
      environment("CFLAGS", "  -Wno-macro-redefined ")

      commandLine(args)
    }

    val buildTaskName = buildTaskName(target)
    xtrasRegisterSourceTask(buildTaskName, target) {
      dependsOn(configureTask)
      outputs.dir(buildDir(target))
      commandLine("make", "install_sw")
      //"make install" requires pod2man which is in /usr/bin/core_perl on archlinux
      //environment("PATH","/usr/bin/core_perl:${environment["PATH"]}")
    }
  }
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

    else -> throw Error("$this not supported for openssl")
  }
