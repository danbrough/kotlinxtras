import org.danbrough.kotlinxtras.binaries.LibraryExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.danbrough.kotlinxtras.enableIconv

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.core")

}


enableIconv()


repositories {
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
      implementation("org.danbrough:klog:_")
      implementation("org.danbrough.kotlinxtras:common:_")
    }
  }

  val posixMain by sourceSets.creating

  targets.withType<KotlinNativeTarget> {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }

    binaries {
      executable("demo") {
        entryPoint = "demo1.main"

      }
    }
  }
}

