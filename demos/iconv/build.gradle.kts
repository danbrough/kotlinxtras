import org.danbrough.kotlinxtras.binaries.LibraryExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
//  id("org.danbrough.kotlinxtras.iconv")
  id("org.danbrough.kotlinxtras.binaries") version "0.0.3-beta12"
  id("org.danbrough.kotlinxtras.iconv") version "0.0.3-beta12"

}


xtrasIconv {
  buildEnabled = true
}

repositories {
  maven("/usr/local/kotlinxtras/build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}


kotlin {

  linuxX64()
  linuxArm32Hfp()
  linuxArm64()
  androidNativeX86()
  macosX64()
  macosArm64()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.org.danbrough.kotlinxtras.common)
    }
  }

  val posixMain by sourceSets.creating

  targets.withType<KotlinNativeTarget> {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }

    binaries {
      executable("iconvDemo") {
        entryPoint = "demo1.main"

      }
    }
  }
}

