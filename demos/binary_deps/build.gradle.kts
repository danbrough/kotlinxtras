import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
  kotlin("multiplatform")
}

val SONA_STAGING = "https://s01.oss.sonatype.org/content/groups/staging/"
val SONA_SNAPSHOTS = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

repositories {
  //for kotlinxtras pre-releases
  maven(SONA_STAGING)
  //for kotlinxtras final releases
  mavenCentral()
}


kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
  androidNativeX86()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.curl)
    }
  }

  val nativeMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  val linuxArm64Main by sourceSets.getting {
    dependencies {

      implementation(libs.curllinuxarm64binaries)
    }
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

tasks.withType<KotlinNativeCompile>().forEach {

}







