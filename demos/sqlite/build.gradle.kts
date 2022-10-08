import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.consumer") version "0.0.1-beta05"
}


repositories {
  //maven("/usr/local/kotlinxtras/build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()
}

//  val preCompiled = configurations.getting
//  dependencies {
//    preCompiled("org.danbrough.kotlinxtras:sqlite:3.39.4")
//  }

binaries {
  enableSqlite()
}

kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
  androidNativeX86()
  androidNativeX64()
  androidNativeArm32()
  androidNativeArm64()
  macosArm64()
  macosX64()

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








