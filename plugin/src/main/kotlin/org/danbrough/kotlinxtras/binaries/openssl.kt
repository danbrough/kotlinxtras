package org.danbrough.kotlinxtras.binaries

import org.jetbrains.kotlin.konan.target.KonanTarget


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
