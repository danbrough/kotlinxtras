import org.danbrough.kotlinxtras.platformName
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.consumer")
}

binaries {


}


repositories {
  //for local builds of the xtras plugins
  maven("../../build/m2")
  //for pre-releases of the xtras plugins
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  //for releases of the xtras plugins
  mavenCentral()
}



kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()


  macosX64()
  macosArm64()

  androidNativeX86()
  androidNativeX64()
  androidNativeArm32()
  androidNativeArm64()


  //add your other apple targets

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.curl)
      implementation(libs.openssl)

    }
  }

  val nativeMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget>().all {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(nativeMain)
    }


    binaries {
      executable("demo1") {
        entryPoint = "demo1.main"
        val konanTarget = target.konanTarget
        runTask?.apply {
          properties["url"]?.also {
            args(it.toString())
          }
          environment("CA_CERT_FILE", file("cacert.pem"))
          val libPath = "${buildDir.resolve("kotlinxtras/curl/${konanTarget.platformName}/lib")}" +
              ":${buildDir.resolve("kotlinxtras/openssl/${konanTarget.platformName}/lib")}"
          environment(
            if (konanTarget.family.isAppleFamily) "DYLD_LIBRARY_PATH" else "LD_LIBRARY_PATH",
            libPath
          )
        }

      }
    }
  }

}



