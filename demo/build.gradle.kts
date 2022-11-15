
import org.danbrough.kotlinxtras.binaries.CurrentVersions.enableIconv
import org.danbrough.kotlinxtras.platformName
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.consumer")
  id("org.danbrough.kotlinxtras.iconv")
}

binaries {
  enableIconv()
}


iconv {
  version = "1.17c"

//  archiveSource("https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz") {
//    stripTopDir = true
//    tarExtractOptions = "xfz"
//  }


  /*
      val args = listOf(
      "./configure", "-C",
      "--enable-static",
      "--host=${target.hostTriplet}",
      "--prefix=${target.iconvPrefix(project)}",
    )
   */



  //gitSource("https://github.com/danbrough/openssl","02e6fd7998830218909cbc484ca054c5916fdc59")

}


kotlin {

  linuxX64()
  linuxArm64()
  androidNativeArm32()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
    dependencies {
      implementation(project(":iconv"))
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