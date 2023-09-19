@file:Suppress("OPT_IN_USAGE")

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
}

repositories {
  mavenCentral()
  gradlePluginPortal()
  google()
}


kotlin {
  linuxX64()
  linuxArm64()
  mingwX64()
  jvm()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.org.danbrough.klog)
    }
  }

  val commonTest by sourceSets.getting
  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.all {

    if (this is KotlinNativeTarget) {
      compilations["main"].apply {
        defaultSourceSet.dependsOn(posixMain)
        cinterops {
          create("curl") {
            defFile = file("curl.def")
          }
        }
      }


      binaries {
        executable("demo") {
          entryPoint = "demo.main"
        }
      }
    }

    if (this is KotlinJvmTarget){
      compilations["main"].apply {

      }

      mainRun {
        mainClass = "demo.DemoKt"
      }
    }
  }
}