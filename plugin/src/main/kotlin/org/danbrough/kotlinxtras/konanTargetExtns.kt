package org.danbrough.kotlinxtras

import org.jetbrains.kotlin.konan.target.Family
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
    else -> TODO("Add hostTriple for $this")

  }