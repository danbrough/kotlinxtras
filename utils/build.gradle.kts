import org.danbrough.kotlinxtras.declareSupportedTargets


plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinxtras.sonatype)
}


version = "0.0.1-beta01"

kotlin {

  declareSupportedTargets()


  sourceSets {
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }


}


