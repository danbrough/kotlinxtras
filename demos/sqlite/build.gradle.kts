import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.consumer")
}


repositories {
  //for local builds
  maven("/usr/local/kotlinxtras/build/m2")
  //for unreleased staging builds
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  //for release builds
  mavenCentral()
}




binaries {
  dependency("org.danbrough.kotlinxtras","sqlite","3.39.4")
  //dependency("org.danbrough.kotlinxtras","curl","7_86_0")
  //dependency("org.danbrough.kotlinxtras","openssl","1_1_1r")
}

kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()

  androidNativeX86()
  androidNativeX64()
  androidNativeArm32()
  androidNativeArm64()


  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.sqlite)
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
      executable("sqliteDemo1") {
        println("LINK TASK: $linkTask type; ${linkTask::class.java}")
        entryPoint = "demo1.main"
        runTask?.apply {
          properties["message"]?.also {
            args(it.toString())
          }
        }
      }
    }
  }
}




