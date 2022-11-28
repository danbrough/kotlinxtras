import org.danbrough.kotlinxtras.binaries.LibraryExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.iconv")
}

xtrasIconv {
  
}

repositories {
  mavenCentral()
  maven(file("../build/m2"))
}


kotlin {

  linuxX64()
  linuxArm32Hfp()
  linuxArm64()

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

