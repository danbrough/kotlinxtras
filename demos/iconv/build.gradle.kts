import org.danbrough.kotlinxtras.binaries.CurrentVersions.enableIconv
import org.danbrough.kotlinxtras.binaries.CurrentVersions.enableSqlite
import org.danbrough.kotlinxtras.platformName
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager


plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.consumer")
}

binaries {
  enableIconv()
}

repositories {
  //maven("/usr/local/kotlinxtras/build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}



kotlin {


  linuxX64()
  linuxArm64()
  androidNativeX86()

  /** //uncomment if you want android support
  androidNativeX86()
  androidNativeX64()
  androidNativeArm32()
  androidNativeArm64()

   **/

  //add your other apple targets


  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.iconv)
    }
  }

  val nativeMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  val native32Main by sourceSets.creating {
    dependsOn(nativeMain)
  }

  val native64Main by sourceSets.creating {
    dependsOn(nativeMain)
  }

  targets.withType<KotlinNativeTarget>().all {

    compilations["main"].apply {
      if (konanTarget.architecture.bitness == 32)
        defaultSourceSet.dependsOn(native32Main)
      else
        defaultSourceSet.dependsOn(native64Main)
    }

    binaries {
      executable("demo1") {
        entryPoint = "demo1.main"
      }
    }

  }
}

