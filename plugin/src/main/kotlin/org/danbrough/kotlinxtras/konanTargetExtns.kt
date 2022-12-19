package org.danbrough.kotlinxtras

import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


private val buildCacheDir = File("/tmp/buildCache")
private val konanDir = File("${System.getProperty("user.home")}/.konan")

private val androidNdkDir = konanDir.resolve(
  if (HostManager.hostIsMac) "dependencies/target-toolchain-2-osx-android_ndk" else
    "dependencies/target-toolchain-2-linux-android_ndk"
)

private val buildPathEnvironment = "/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/opt/local/bin"

private const val androidNdkApiVersion = 21

val KonanTarget.platformName: String
  get() {
    if (family == Family.ANDROID) {
      return when (this) {
        KonanTarget.ANDROID_X64 -> "androidNativeX64"
        KonanTarget.ANDROID_X86 -> "androidNativeX86"
        KonanTarget.ANDROID_ARM64 -> "androidNativeArm64"
        KonanTarget.ANDROID_ARM32 -> "androidNativeArm32"
        else -> throw Error("Unhandled android target $this")
      }
    }
    return name.split("_").joinToString("") { it.capitalize() }.decapitalize()
  }


val KonanTarget.hostTriplet: String
  get() = when (this) {
    KonanTarget.LINUX_ARM64 -> "aarch64-unknown-linux-gnu"
    KonanTarget.LINUX_X64 -> "x86_64-unknown-linux-gnu"
    KonanTarget.LINUX_ARM32_HFP -> "arm-linux-gnueabihf"
    KonanTarget.ANDROID_ARM32 -> "armv7a-linux-androideabi"
    KonanTarget.ANDROID_ARM64 -> "aarch64-linux-android"
    KonanTarget.ANDROID_X64 -> "x86_64-linux-android"
    KonanTarget.ANDROID_X86 -> "i686-linux-android"
    KonanTarget.MACOS_X64 -> "x86_64-apple-darwin"

    KonanTarget.MACOS_ARM64 -> "aarch64-apple-darwin"
    KonanTarget.MINGW_X64 -> "x86_64-w64-mingw32"
    KonanTarget.MINGW_X86 -> "x86-w64-mingw32"
    KonanTarget.IOS_ARM32 -> "arm32-apple-darwin"
    KonanTarget.IOS_ARM64 -> "aarch64-ios-darwin"
    KonanTarget.IOS_SIMULATOR_ARM64 -> "aarch64-iossimulator-darwin"
    KonanTarget.IOS_X64 -> "x86_64-ios-darwin"


    KonanTarget.TVOS_ARM64 -> "aarch64-tvos-darwin"
    KonanTarget.TVOS_SIMULATOR_ARM64 -> "aarch64-tvossimulator-darwin"
    KonanTarget.TVOS_X64 -> "x86_64-tvos-darwin"
    KonanTarget.WASM32 -> TODO()
    KonanTarget.WATCHOS_ARM32 -> "arm32-watchos-darwin"
    KonanTarget.WATCHOS_ARM64 -> "aarch64-watchos-darwin"
    KonanTarget.WATCHOS_SIMULATOR_ARM64 -> "aarch64-watchossimulator-darwin"
    KonanTarget.WATCHOS_X64 -> "x86_64-watchos-darwin"
    KonanTarget.WATCHOS_X86 -> "x86-watchos-darwin"
    else -> TODO("Add KonanTarget.hostTriplet for $this")

  }


val KonanTarget.androidLibDir: String?
  get() = when (this) {
    KonanTarget.ANDROID_ARM32 -> "armeabi-v7a"
    KonanTarget.ANDROID_ARM64 -> "arm64-v8a"
    KonanTarget.ANDROID_X64 -> "x86_64"
    KonanTarget.ANDROID_X86 -> "x86"
    else -> null
  }

val KonanTarget.sharedLibExtn: String
  get() = when {
    family.isAppleFamily -> "dylib"
    family == Family.MINGW -> "dll"
    else -> "so"
  }


val KonanTarget.goOS: String?
  get() = when (family) {
    Family.OSX -> "darwin"
    Family.IOS, Family.TVOS, Family.WATCHOS -> "ios"
    Family.LINUX -> "linux"
    Family.MINGW -> "windows"
    Family.ANDROID -> "android"
    Family.WASM -> null
    Family.ZEPHYR -> null
  }

val KonanTarget.goArch: String
  get() = when (architecture) {
    Architecture.ARM64 -> "arm64"
    Architecture.X64 -> "amd64"
    Architecture.X86 -> "386"
    Architecture.ARM32 -> "arm"
    Architecture.MIPS32 -> "mips" //TODO: confirm this
    Architecture.MIPSEL32 -> "mipsle" //TODO: confirm this
    Architecture.WASM32 -> "wasm"
  }



fun KonanTarget.buildEnvironment(): MutableMap<String, *> = mutableMapOf(
  "CGO_ENABLED" to 1, "GOARM" to 7, "GOOS" to goOS, "GOARCH" to goArch,
  "GOBIN" to buildCacheDir.resolve("$name/bin"),
  "GOCACHE" to buildCacheDir.resolve("$name/gobuild"),
  "GOCACHEDIR" to buildCacheDir.resolve("$name/gocache"),
  "GOMODCACHE" to buildCacheDir.resolve("gomodcache"),
  "GOPATH" to buildCacheDir.resolve(name),
  "KONAN_DATA_DIR" to konanDir,
  "CFLAGS" to "-O3 -pthread -Wno-macro-redefined -Wno-deprecated-declarations ",//-DOPENSSL_SMALL_FOOTPRINT=1",
  "MAKE" to "make -j4",
).apply {
  val path = buildPathEnvironment.split(':').toMutableList()

  this["KONAN_BUILD"] = 1

  this["ANDROID_NDK_HOME"] = androidNdkDir.absolutePath


  when (this@buildEnvironment) {

    KonanTarget.LINUX_ARM32_HFP -> {
      val clangArgs =
        "--target=$hostTriplet --gcc-toolchain=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2/arm-unknown-linux-gnueabihf/sysroot "
      this["CC"] = "clang $clangArgs"
      this["CXX"] = "clang++ $clangArgs"
    }

    KonanTarget.LINUX_ARM64 -> {
      val clangArgs =
        "--target=$hostTriplet --gcc-toolchain=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2 --sysroot=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/aarch64-unknown-linux-gnu/sysroot"
      this["CC"] = "clang $clangArgs"
      this["CXX"] = "clang++ $clangArgs"
    }

    KonanTarget.LINUX_X64 -> {
      val clangArgs =
        "--target=$hostTriplet --gcc-toolchain=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot"
      this["CC"] = "clang $clangArgs"
      this["CXX"] = "clang++ $clangArgs"
      /*        this["RANLIB"] =
                "$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/bin/ranlib"*/
    }

    KonanTarget.MACOS_X64,KonanTarget.MACOS_ARM64 -> {
      this["CC"] = "gcc"
      this["CXX"] = "g++"
      this["LD"] = "lld"
    }


    KonanTarget.MINGW_X64 -> {
//        this["CC"] = "x86_64-w64-mingw32-gcc"
//        this["CXX"] = "x86_64-w64-mingw32-g++"

//        val clangArgs =
//          "--target=$hostTriplet --gcc-toolchain=$konanDir/dependencies/msys2-mingw-w64-x86_64-2/x86_64-w64-mingw32" +
//              " --sysroot=$konanDir/dependencies/msys2-mingw-w64-x86_64-2/x86_64-w64-mingw32/x86_64-w64-mingw32"
//        this["CC"] = "clang $clangArgs"
//        this["CXX"] = "clang++ $clangArgs"
      /*  export HOST=x86_64-w64-mingw32
export GOOS=windows
export CFLAGS="$CFLAGS -pthread"
#export WINDRES=winres
export WINDRES=/usr/bin/x86_64-w64-mingw32-windres
export RC=$WINDRES
export GOARCH=amd64
export OPENSSL_PLATFORM=mingw64
export LIBNAME="libkipfs.dll"
#export PATH=/usr/x86_64-w64-mingw32/bin:$PATH
export TARGET=$HOST
#export PATH=$(dir_path bin $TOOLCHAIN):$PATH
export CROSS_PREFIX=$TARGET-
export CC=$TARGET-gcc
export CXX=$TARGET-g++
      */
      /*
              this["WINDRES"] = "x86_64-w64-mingw32-windres"
              this["RC"] = this["WINDRES"] as String*/
      /*this["CROSS_PREFIX"] = "${platform.host}-"
      val toolChain = "$konanDir/dependencies/msys2-mingw-w64-x86_64-1"
      this["PATH"] = "$toolChain/bin:${this["PATH"]}"*/

      //this["CC"] = "x86_64-w64-mingw32-gcc"
      //this["CXX"] = "x86_64-w64-mingw32-g++"


    }

    KonanTarget.ANDROID_X64, KonanTarget.ANDROID_X86, KonanTarget.ANDROID_ARM64, KonanTarget.ANDROID_ARM32 -> {
      path.add(0, androidNdkDir.resolve("bin").absolutePath)
      this["CC"] = "$hostTriplet${androidNdkApiVersion}-clang"
      this["CXX"] = "$hostTriplet${androidNdkApiVersion}-clang++"
      this["AR"] = "llvm-ar"
      this["RANLIB"] = "ranlib"
    }
  }

  if (HostManager.hostIsMac)
    path.add(0, konanDir.resolve("dependencies/apple-llvm-20200714-macos-x64-essentials/bin").absolutePath)
  path.add(0, konanDir.resolve("dependencies/llvm-11.1.0-linux-x64-essentials/bin").absolutePath)
  this["PATH"] = path.joinToString(File.pathSeparator)
}







