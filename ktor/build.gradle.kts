import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
}

kotlin {
  linuxArm64()

  targets.withType<KotlinNativeTarget>().all {
    /*compilations["test"].apply {
      defaultSourceSet.dependsOn(commonTest)
    }*/
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("org.danbrough.ktor:ktor:2.1.0")
      }
    }
  }
}

repositories {

}