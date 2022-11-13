@file:Suppress("DEPRECATION")

package org.danbrough.kotlinxtras

import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

const val xtrasTaskGroup = "xtras"

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

object OpenSSL {

  const val GIT_SRC = "https://github.com/danbrough/openssl"

  fun KonanTarget.opensslSrcDir(project: Project): File =
    project.rootProject.file("openssl/build/openssl/$platformName")

  fun KonanTarget.opensslPrefix(project: Project): File =
    project.rootProject.file("libs/openssl/$platformName")

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
}



val KonanTarget.konanDepsTaskName: String
  get() = ":common:compileKotlin${platformName.capitalized()}"
