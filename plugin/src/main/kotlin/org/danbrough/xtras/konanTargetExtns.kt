package org.danbrough.xtras

import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget


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
    return name.split("_").joinToString("") { it.capitalized() }.decapitalized()
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

val SHARED_LIBRARY_PATH_NAME = if (HostManager.hostIsMac) "DYLD_LIBRARY_PATH" else "LD_LIBRARY_PATH"


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









