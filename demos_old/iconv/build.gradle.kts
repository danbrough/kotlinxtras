import org.danbrough.kotlinxtras.core.enableIconv
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.core")

}


enableIconv {
  cinterops {

  }
}
repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}


kotlin {

  linuxX64()
  linuxArm32Hfp()
  linuxArm64()
  androidNativeX86()
  macosX64()
  macosArm64()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation("org.danbrough.kotlinxtras:common:_")
    }
  }


  val posixMain by sourceSets.creating

  targets.withType<KotlinNativeTarget> {

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

tasks.create("run") {
  dependsOn("runIconvDemoDebugExecutable${if (HostManager.hostIsMac) "MacosX64" else "LinuxX64"}")
}

