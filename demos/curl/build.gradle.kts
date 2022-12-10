import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.curl")
  id("org.danbrough.kotlinxtras.openssl")

}


repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}

xtrasCurl {
  cinterops {
    interopsPackage = "libcurl"
  }
}

kotlin {

  linuxX64()
  linuxArm32Hfp()
  linuxArm64()

  macosX64()
  macosArm64()
  androidNativeX86()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation("org.danbrough:klog:_")
      implementation("org.danbrough.kotlinxtras:common:_")
    }
  }

  val posixMain by sourceSets.creating

  targets.withType<KotlinNativeTarget> {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }


    binaries {
      executable("curlDemo") {
        entryPoint = "demo1.main"

      }
    }
  }
}

