
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.consumer")
}



repositories {
  maven("../../build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}

binaries {
  enableCurl(version = libs.curl.get().versionConstraint.toString())
  enableOpenSSL(version = libs.openssl.get().versionConstraint.toString())
}


tasks.register("thang") {
  doFirst {
    println("VERSION: ${libs.curl.get().versionConstraint}")
  }
}


kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
  // uncomment if you want them
  //macosX64()
  //macosArm64()

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
      implementation(libs.ktor.server.cio)
      implementation(libs.kotlinx.datetime)

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


