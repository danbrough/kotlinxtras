import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  kotlin("multiplatform")
}

val SONA_STAGING = "https://s01.oss.sonatype.org/content/groups/staging/"
val SONA_SNAPSHOTS = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

repositories {
  maven(project.file("../../build/m2")) {
    name = "m2"
  }
  maven(SONA_STAGING)
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







