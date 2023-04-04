import org.danbrough.kotlinxtras.core.enableCurl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.core")
}


enableCurl {

}

repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}


kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
  macosX64()


  /** //uncomment if you want android support
  androidNativeX86()
  androidNativeX64()
  androidNativeArm32()
  androidNativeArm64()

   **/

  //add your other apple targets


  val commonMain by sourceSets.getting {
    dependencies {
      implementation("org.danbrough:klog:_")
      implementation("org.danbrough.ktor:ktor-client-curl:_")

      implementation("org.danbrough.kotlinx:kotlinx-coroutines-core:_")
      implementation("org.danbrough.ktor:ktor-server-cio:_")
      implementation("org.danbrough.kotlinx:kotlinx-datetime:_")

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
      executable("demo2") {
        entryPoint = "demo2.main"
      }
    }
  }
}


