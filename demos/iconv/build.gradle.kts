
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.consumer")
}



repositories {
  //maven("/usr/local/kotlinxtras/build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}

binaries {
//  addBinaryDependency(
//    org.danbrough.kotlinxtras.binaries.BinDep(
//      "org.danbrough.kotlinxtras",
//      "iconv",
//      "1.17_01"
//    )
//  )
 enableIconv()
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
      implementation(libs.iconv)

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


