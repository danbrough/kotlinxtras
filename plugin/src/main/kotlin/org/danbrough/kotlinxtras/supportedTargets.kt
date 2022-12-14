package org.danbrough.kotlinxtras

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.konan.target.KonanTarget

/**
 * Declares the kotlin native targets that are currently supported
 */
fun KotlinMultiplatformExtension.declareSupportedTargets() {
  //comment out platforms you don't need
  androidNativeX86()
  androidNativeX64()
  androidNativeArm32()
  androidNativeArm64()

  mingwX64()

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()

  macosX64()
  macosArm64()

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
