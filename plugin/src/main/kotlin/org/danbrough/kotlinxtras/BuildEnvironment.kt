@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.kotlinxtras

import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


class BuildEnvironment(library: XtrasLibrary) {

  inner class Binaries {
    var git = "git"
    var tar = "tar"
    var autoreconf = "autoreconf"
  }

  init {
    library.project.log("created BuildEnvironment for $library")
  }

  val binaries = Binaries()

  var konanDir: File = System.getenv("KONAN_DATA_DIR")?.let { File(it) } ?: File(
    System.getProperty("user.home"),
    ".konan"
  )

  @XtrasDSLMarker
  var basePath: List<String> =
    listOf("/bin", "/sbin", "/usr/bin", "/usr/sbin", "/usr/local/bin", "/opt/local/bin")

  @XtrasDSLMarker
  var androidNdkApiVersion = 21

  @XtrasDSLMarker
  var defaultEnvironment: Map<String, String> = buildMap {

    put("PATH", basePath.joinToString(File.pathSeparator))

    put("MAKE","make -j${Runtime.getRuntime().availableProcessors()+1}")

    put("KONAN_BUILD", "1")
  }

  @XtrasDSLMarker
  val runningInIDEA = (System.getProperty("idea.active") != null).also {
    library.project.log("RUNNING IN IDE: $it")
  }

  @XtrasDSLMarker
  var androidNdkDir: File? = null

  private fun androidNdkDir(): File {
    androidNdkDir?.also {
      return it
    }
    val ndkRoot = System.getenv("ANDROID_NDK_ROOT") ?: System.getenv("ANDROID_NDK_HOME")
    if (ndkRoot != null) return File(ndkRoot).also {
      androidNdkDir = it
    }
    error("Neither ANDROID_NDK_ROOT or ANDROID_NDK_HOME are set!")
  }

  @XtrasDSLMarker
  var environmentForTarget: MutableMap<String, String>.(KonanTarget) -> Unit = { target ->

    if (!HostManager.hostIsMac || !target.family.isAppleFamily) {
      val llvmPrefix = if (HostManager.hostIsLinux) "llvm-" else "apple-llvm"
      konanDir.resolve("dependencies").listFiles()
        ?.firstOrNull { it.isDirectory && it.name.startsWith(llvmPrefix) }?.also {
          put("PATH", "${it.resolve("bin").absolutePath}:${get("PATH")}")
        }
    }

    var clangArgs: String? = null

    val toolChainVersion = "gcc-8.3.0-glibc-2.25-kernel-4.9-2"

    when (target) {
      KonanTarget.LINUX_ARM64 -> {
        clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/aarch64-unknown-linux-gnu-$toolChainVersion --sysroot=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/aarch64-unknown-linux-gnu/sysroot"
      }

      KonanTarget.LINUX_X64 -> {
        if (HostManager.hostIsLinux)
          clangArgs =
            "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/x86_64-unknown-linux-gnu-$toolChainVersion --sysroot=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot"
      }

      KonanTarget.LINUX_ARM32_HFP -> {
        if (HostManager.hostIsLinux)
          clangArgs =
            "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/arm-unknown-linux-gnueabihf-$toolChainVersion --sysroot=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2/arm-unknown-linux-gnueabihf/sysroot"
      }

//dan /usr/local/kotlinxtras $ ls ~/.konan/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2/arm-unknown-linux-gnueabihf/
//bin/        debug-root/ include/    lib/        sysroot/
//dan@dan /usr/local/kotlinxtras $ ls ~/.konan/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2/arm-unknown-linux-gnueabihf/sysroot/

      KonanTarget.MACOS_X64, KonanTarget.MACOS_ARM64, KonanTarget.WATCHOS_X64, KonanTarget.WATCHOS_ARM64, KonanTarget.IOS_X64, KonanTarget.IOS_ARM64 -> {
        put("CC", "clang")
        //put("CXX", "g++")
        //put("LD", "lld")
      }

      KonanTarget.MINGW_X64-> {

      }

      KonanTarget.ANDROID_X64, KonanTarget.ANDROID_X86, KonanTarget.ANDROID_ARM64, KonanTarget.ANDROID_ARM32 -> {
        //library.project.log("ADDING NDK TO PATH")
        val archFolder = if (HostManager.hostIsLinux) "linux-x86_64" else "darwin-x86_64"
        put(
          "PATH",
          "${androidNdkDir().resolve("toolchains/llvm/prebuilt/$archFolder/bin").absolutePath}:${
            get("PATH")
          }"
        )


        //basePath.add(0, androidNdkDir.resolve("bin").absolutePath)
        put("CC", "${target.hostTriplet}${androidNdkApiVersion}-clang")
        put("CXX", "${target.hostTriplet}${androidNdkApiVersion}-clang++")
        put("AR", "llvm-ar")
        put("RANLIB", "ranlib")

      }

      else -> error("Unhandled target: $target")
    }

    if (clangArgs != null) {
      put("CC", "clang $clangArgs")
      put("CXX", "clang++ $clangArgs")
    }
  }

  fun getEnvironment(target: KonanTarget? = null) = buildMap {
    putAll(defaultEnvironment)
    if (target != null)
      environmentForTarget(target)
  }

  @XtrasDSLMarker
  fun binaries(config: Binaries.() -> Unit) {
    binaries.config()
  }
}


