import BuildEnvironment.platformName
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  kotlin("multiplatform")
}

ProjectProperties.init(project)

repositories {
  maven(project.file("../../build/m2")) {
    name = "m2"
  }
  maven(Dependencies.SONA_STAGING)
  mavenCentral()
}


kotlin {
  linuxX64()
  linuxArm64()
  linuxArm32Hfp()

  sourceSets {

    val commonMain by getting {
      dependencies{
        implementation(Dependencies.klog)
        implementation("org.danbrough.ktor:ktor-client-curl:2.1.0")
      }
    }
  }

  targets.withType<KotlinNativeTarget>().all {

    binaries {
      executable("demo1"){
        entryPoint = "demo1.main"
      }
    }
  }
}







