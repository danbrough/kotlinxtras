import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform") version "1.6.21"
  id("org.danbrough.kotlinxtras.xtras") version "0.0.3-beta02"
  id("org.danbrough.kotlinxtras.iconv") version "0.0.3-beta02"

}


/*
iconv {

}
*/

repositories {
  mavenCentral()
}


kotlin {


  linuxX64()
  linuxArm32Hfp()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation("org.danbrough:klog:0.0.2-beta01")
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