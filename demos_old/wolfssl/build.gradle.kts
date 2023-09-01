@file:Suppress("OPT_IN_USAGE", "UnstableApiUsage")


import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras)
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


  jvm{
    mainRun {
      mainClass = "demo1.MainKt"
    }
  }

  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
  }

  //androidNativeX86()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.org.danbrough.klog)


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





