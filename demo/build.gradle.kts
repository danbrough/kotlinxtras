

import org.danbrough.kotlinxtras.binaries.configureSources
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.platformName
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform") version "1.6.21"
  id("org.danbrough.kotlinxtras.iconv") version "0.0.1"
}


iconv {


}
repositories {
  mavenCentral()
}


kotlin {


  linuxX64()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
    dependencies {
//      implementation(project(":iconv"))
    }
  }

  targets.withType<KotlinNativeTarget>().all {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }

    binaries {
      executable("iconvDemo") {
        entryPoint = "demo1.main"
      }
    }
  }

}