
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.danbrough.kotlinxtras.enableSqlite

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.core")
}

enableSqlite {

}


repositories {
  //for local builds
  maven("/usr/local/kotlinxtras/build/m2")
  //for unreleased staging builds
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  //for release builds
  mavenCentral()
}


kotlin {

  if (org.jetbrains.kotlin.konan.target.HostManager.Companion.hostIsMac){
    macosArm64()
    macosX64()
  } else {
    linuxX64()
    linuxArm64()
    linuxArm32Hfp()
    androidNativeX86()
  }

  val commonMain by sourceSets.getting {
    dependencies {
      implementation("org.danbrough:klog:_")
      implementation("org.danbrough.kotlinx:kotlinx-coroutines-core:_")
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
      executable("sqliteDemo") {
        println("LINK TASK: $linkTask type; ${linkTask::class.java}")
        entryPoint = "demo.main"
        runTask?.apply {
          properties["message"]?.also {
            args(it.toString())
          }
        }
      }
    }
  }
}




