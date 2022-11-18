package org.danbrough.kotlinxtras.binaries

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Declares the kotlin native targets that are currently supported
 */
fun KotlinMultiplatformExtension.declareNativeTargets() {
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


