import org.danbrough.kotlinxtras.configurePrecompiledBinaries
import org.danbrough.kotlinxtras.platformName
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.xtras")
}



repositories {

  maven("../../build/m2") {
    name = "m2"
  }

  //maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()

}

xtras {
  autoExtractBinaries = true
}


kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
  androidNativeX86()

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

    compilations["main"].defaultSourceSet.dependsOn(nativeMain)

    binaries {
      executable("demo1") {
        entryPoint = "demo1.main"
      }
    }
  }

}



afterEvaluate{
  project.configurePrecompiledBinaries()
}