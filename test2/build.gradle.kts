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
//  linuxX64()
//  linuxArm64()
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
          if (project.hasProperty("url"))
            runTask!!.args = listOf(project.property("url")!!.toString())
          runTask!!.apply {
            environment("PATH","/c/xtras/xtras/libs/curl/8.3.0/mingwX64/bin:${environment["PATH"]}")
            environment("CA_CERT_FILE",project.file("cacert.pem"))

          }
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