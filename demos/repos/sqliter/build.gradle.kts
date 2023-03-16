import org.danbrough.kotlinxtras.core.enableSqlite
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.core")
}

repositories {
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()
}

enableSqlite()


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
      implementation("org.danbrough:klog:_")
      implementation("org.danbrough.kotlinx:kotlinx-coroutines-core:_")
      implementation("org.danbrough.sqldelight:native-driver:_")
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








