import org.danbrough.kotlinxtras.binaries.download
import org.danbrough.kotlinxtras.binaries.git
import org.danbrough.kotlinxtras.binaries.registerLibraryExtension
import org.danbrough.kotlinxtras.core.enableCurl
import org.danbrough.kotlinxtras.core.enableLibSSH2
import org.danbrough.kotlinxtras.core.enableWolfSSL
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.platformName
import org.gradle.configurationcache.extensions.capitalized

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtrasCore)
  alias(libs.plugins.kotlinXtrasBinaries)
}


repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
  google()
}


kotlin {

  linuxX64()
  androidNativeArm64()
  linuxArm64()
  //jvm()

  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
  }

  //androidNativeX86()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.org.danbrough.kotlinxtras.common)
      implementation(libs.org.danbrough.kotlinxtras.utils)

      //implementation(libs.io.ktor.ktorutils)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }



  targets.withType<KotlinNativeTarget> {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }

    binaries {
      executable("demo1") {
        entryPoint = "demo1.main"
        findProperty("args")?.also {
          runTask?.args(it.toString().split(','))
        }
      }
    }
  }
}





