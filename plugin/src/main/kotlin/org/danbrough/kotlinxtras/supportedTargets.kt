package org.danbrough.kotlinxtras

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

/**
 * Declares the kotlin native targets that are currently supported
 *
 * The following targets are marked as deprecated in 1.8.20 and will be removed in 1.9.20:
 *
 *     iosArm32 – 32-bit iOS Apps were deprecated a long time ago, and the last iOS release that supported them was iOS 10, which was released back in 2016. Furthermore, as of version 14 the most recent release, Xcode no longer builds 32-bit binaries.
 *     watchosX86 – This is an obsolete simulator for Intel Macs. Use the watchosX64 target instead.
 *     linuxArm32Hfp – When it comes to 32-bit ARM, it is hard to provide a single universal Kotlin/Native target because of the variety of options: ARMv5, ARMv6, ARMv7; hfp, sfp; multiple toolchains; etc. To some extent, this is true for other Linux targets, but linuxArm32Hfp has proven to be especially hard to support.
 *     linuxMips32 and linuxMipsel32 – We haven’t seen any real demand for these two.
 *     mingwX86 – Support for this target requires a lot of resources while user demand remains low. Furthermore, mingwX64 is better suited for the majority of known use cases anyway.
 *     wasm32 – We’re deprecating this one in favor of the new fully-fledged Kotlin/Wasm toolchain. Check it out!
 *
 */
fun KotlinMultiplatformExtension.declareSupportedTargets() {
  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
    iosArm64()
    iosX64()
    watchosX64()
  } else {
    androidNativeX86()
    androidNativeX64()
    androidNativeArm32()
    androidNativeArm64()

    mingwX64()

    linuxX64()
    linuxArm64()
    linuxArm32Hfp()
  }


  /*
  //TODO
  iosArm32()
  iosArm64()
  iosSimulatorArm64()
  iosX64()

  tvosArm64()
  tvosX64()
  tvosSimulatorArm64()

  watchosArm32()
  watchosArm64()
  watchosX64()
  watchosX86()
  watchosSimulatorArm64()
   */

}

val xtrasSupportedTargets: List<KonanTarget> = listOf(
  KonanTarget.LINUX_X64,
  KonanTarget.LINUX_ARM32_HFP,
  KonanTarget.LINUX_ARM64,
  KonanTarget.ANDROID_ARM32,
  KonanTarget.ANDROID_ARM64,
  KonanTarget.ANDROID_X64,
  KonanTarget.ANDROID_X86,
  KonanTarget.IOS_X64,
  KonanTarget.IOS_ARM64,
  KonanTarget.WATCHOS_X64,
  KonanTarget.WATCHOS_ARM64,
  KonanTarget.MACOS_X64,
  KonanTarget.MACOS_ARM64,
  KonanTarget.MINGW_X64,
)


/**
 * Declare target support for the host platform
 */
fun KotlinMultiplatformExtension.declareHostTarget(configure: KotlinNativeTarget.() -> Unit = { }) {
  when (HostManager.host) {
    KonanTarget.MACOS_X64 -> macosX64(configure = configure)
    KonanTarget.MACOS_ARM64 -> macosArm64(configure = configure)
    KonanTarget.LINUX_ARM64 -> linuxArm64(configure = configure)
    KonanTarget.LINUX_X64 -> linuxX64(configure = configure)
    KonanTarget.MINGW_X64 -> mingwX64(configure = configure)
    else -> error("Unhandled host platform: ${HostManager.host}")
  }
}