import org.danbrough.kotlinxtras.declareSupportedTargets


plugins {
  kotlin("multiplatform")
  xtras("sonatype")
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


