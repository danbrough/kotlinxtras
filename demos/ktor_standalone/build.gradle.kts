
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.consumer")
}



repositories {
 // maven("../../build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}

binaries {
  enableCurl()
  enableOpenSSL()
}


kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
  macosX64()
  macosArm64()

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
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.curl)
      implementation(libs.kotlinx.coroutines.core)
    }
  }


  val nativeMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget>().all {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(nativeMain)
    }

    binaries {
      executable("demo1") {
        entryPoint = "demo1.main"
      }
    }
  }
}


