import org.danbrough.kotlinxtras.configurePrecompiledBinaries
import org.danbrough.kotlinxtras.Repositories
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.xtras")
}


repositories {
  maven(Repositories.SONA_STAGING)
  mavenCentral()
}


kotlin {


  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
  macosX64()
  macosArm64()
  androidNativeArm64()
  androidNativeArm32()
  androidNativeX86()
  androidNativeX64()



  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.curl)
      implementation(libs.kotlinx.coroutines.core)
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
      }
    }
  }
}

xtras {
  enableCurl()
  enableOpenSSL()
}


afterEvaluate{
  configurePrecompiledBinaries()
}




