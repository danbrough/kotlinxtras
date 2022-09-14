import BuildEnvironment.platformName
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  kotlin("multiplatform")
}

ProjectProperties.init(project)

repositories {
  maven(Dependencies.SONA_STAGING)
  mavenCentral()
}


kotlin {


  linuxX64()
  linuxArm64()
  linuxArm32Hfp()


  val commonMain by sourceSets.getting {
    dependencies {
      implementation(Dependencies.klog)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.curl)
      implementation(libs.kotlinx.coroutines.core)
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







