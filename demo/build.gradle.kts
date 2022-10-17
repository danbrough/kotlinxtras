
import org.danbrough.kotlinxtras.hostIsMac
import org.danbrough.kotlinxtras.platformName
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.consumer")
}


binaries {
  enableCurl()
  enableOpenSSL()
  enableSqlite()
}

repositories {
  maven("../build/m2")
}

kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()

  if (hostIsMac) {
    macosX64()
    macosArm64()
    //add your other apple targets

  }

  /** //uncomment if you want android support
  androidNativeX86()
  androidNativeX64()
  androidNativeArm32()
  androidNativeArm64()

   **/


  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(project(":curl"))
      implementation(project(":openssl"))
      implementation(project(":sqlite"))
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
      executable("curlDemo") {
        entryPoint = "demo.curl.main"
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

      executable("sqliteDemo") {
        entryPoint = "demo.sqlite.main"
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

