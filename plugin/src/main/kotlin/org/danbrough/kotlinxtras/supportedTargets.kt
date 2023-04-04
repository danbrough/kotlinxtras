package org.danbrough.kotlinxtras

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

/**
 * Declares the kotlin native targets that are currently supported
 */
fun KotlinMultiplatformExtension.declareSupportedTargets() {
  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
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
    else -> throw Error("Unhandled host platform: ${HostManager.host}")
  }
}