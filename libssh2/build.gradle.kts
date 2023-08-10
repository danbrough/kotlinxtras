import org.danbrough.kotlinxtras.core.enableLibSSH2
import org.danbrough.kotlinxtras.core.enableOpenssl3
import org.danbrough.kotlinxtras.declareSupportedTargets

plugins {
  // `kotlin-dsl`
  //kotlin("multiplatform")
  kotlin("multiplatform")

  xtras("sonatype", Xtras.version)
  xtras("core", Xtras.version)
}
version = "0.0.1-beta01"



enableLibSSH2(enableOpenssl3()) {
  deferToPrebuiltPackages = true

  cinterops {
    interopsPackage = "$group.${project.name}"
  }
}

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


