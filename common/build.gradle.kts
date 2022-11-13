
import org.danbrough.kotlinxtras.BuildEnvironment.declareNativeTargets
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  `maven-publish`
}

kotlin {
  declareNativeTargets()

  val posixMain by sourceSets.creating {}

  val posix32Main by sourceSets.creating {
    dependsOn(posixMain)
  }

  val posix64Main by sourceSets.creating {
    dependsOn(posixMain)
  }

  targets.withType<KotlinNativeTarget>().all {
    compilations["main"].apply {
      if (konanTarget.architecture.bitness == 32)
        defaultSourceSet.dependsOn(posix32Main)
      else
        defaultSourceSet.dependsOn(posix64Main)
    }
  }
}