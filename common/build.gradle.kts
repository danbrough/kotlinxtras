import org.danbrough.xtras.declareSupportedTargets
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinxtras.sonatype)
}


version = "0.0.1-beta01"

kotlin {

  declareSupportedTargets()
  jvm()

  val posixMain by sourceSets.creating

  val posix32Main by sourceSets.creating {
    dependsOn(posixMain)
  }

  val posix64Main by sourceSets.creating {
    dependsOn(posixMain)
  }

  sourceSets {
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }

  targets.withType<KotlinNativeTarget>().all {
    compilations["main"].apply {
      if (konanTarget.architecture.bitness == 32) defaultSourceSet.dependsOn(posix32Main)
      else defaultSourceSet.dependsOn(posix64Main)
    }
  }


}


