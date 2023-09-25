package org.danbrough.xtras.openssl

import org.danbrough.xtras.XtrasDSLMarker
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.library.XtrasLibrary
import org.danbrough.xtras.library.xtrasCreateLibrary
import org.danbrough.xtras.library.xtrasRegisterSourceTask
import org.danbrough.xtras.log
import org.danbrough.xtras.source.gitSource
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

const val OPENSSL_VERSION = "3.1.3"
const val OPENSSL_COMMIT = "openssl-3.1.3"

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
  version: String = properties.getOrDefault("openssl.version", OPENSSL_VERSION).toString(),
  commit: String = properties.getOrDefault("openssl.commit", OPENSSL_COMMIT).toString(),
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

    /*
        val configureTask = xtrasRegisterSourceTask(XtrasLibrary.TaskName.CONFIGURE, target) {
      dependsOn(libraryDeps.map { it.extractArchiveTaskName(target) })
      dependsOn(prepareSourceTask)
      outputs.file(workingDir.resolve("Makefile"))
     */
    val configureTask = xtrasRegisterSourceTask(XtrasLibrary.TaskName.CONFIGURE, target) {
      dependsOn(libraryDeps.map { it.extractArchiveTaskName(target) })
      outputs.file(workingDir.resolve("Makefile"))
      val args = mutableListOf(
        "./Configure",
        target.opensslPlatform,
        "no-tests",
        "threads",
        "--prefix=${buildDir(target).absolutePath.replace('\\', '/')}",
        "--libdir=lib",
      )

      if (target.family == Family.ANDROID) args += "-D__ANDROID_API__=21"
      /*      else if (target.family == Family.MINGW) args += "--cross-compile-prefix=${target.hostTriplet}-"
            environment("CFLAGS", "  -Wno-macro-redefined ")*/

      if (HostManager.hostIsMingw) commandLine(
        buildEnvironment.binaries.bash, "-c", args.joinToString(" ")
      )
      else commandLine(args)
    }

    xtrasRegisterSourceTask(XtrasLibrary.TaskName.BUILD, target) {
      doFirst {
        project.log("running make install with CC=${environment["CC"]}")
      }
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
    //KonanTarget.IOS_ARM32 -> "ios-cross" //ios-cross ios-xcrun ios64-cross ios64-xcrun iossimulator-xcrun iphoneos-cross

    KonanTarget.IOS_ARM64 -> "ios64-cross" //ios-cross ios-xcrun
    KonanTarget.IOS_SIMULATOR_ARM64 -> "iossimulator-xcrun"
    KonanTarget.IOS_X64 -> "ios64-xcrun"

    else -> throw Error("$this not supported for openssl")
  }
