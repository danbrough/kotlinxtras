import org.danbrough.kotlinxtras.Repositories
import org.danbrough.kotlinxtras.configurePrecompiledBinaries
import org.danbrough.kotlinxtras.platformName
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.xtras")
}



repositories {
  //maven("../../build/m2")
  maven(Repositories.SONA_STAGING)
  mavenCentral()
}

xtras {
  enableCurl()
  enableOpenSSL()
}


kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
  //not bothering to support curl-client on android as yet
  macosX64()
  macosArm64()
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
          environment(if (konanTarget.family.isAppleFamily) "DYLD_LIBRARY_PATH" else "LD_LIBRARY_PATH",libPath)
        }

      }
    }
  }

}

afterEvaluate {
  configurePrecompiledBinaries()
}


