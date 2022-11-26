import org.danbrough.kotlinxtras.binaries.LibraryExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.iconv")
  id("org.danbrough.kotlinxtras.openssl")

}

iconv {
  cinterops {
    headersFile = file("src/cinterops/libiconv_header.def")
  }
}


repositories {
  mavenCentral()
  maven(file("../build/m2"))
}


kotlin {

  linuxX64()
  linuxArm32Hfp()
  linuxArm64()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.org.danbrough.kotlinxtras.common)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
    dependencies {
//      implementation(project(":iconv"))
    }
  }

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

afterEvaluate {

  val xtras = extensions.getByType(LibraryExtension::class)

  kotlin.targets.withType<KotlinNativeTarget> {
    binaries.all {
      if (this is Executable) {
        //println("EXECUTABLE $name ${this.buildType} ${outputKind} ${this.runTaskName}")
        val ldLibraryPathKey = if (HostManager.hostIsMac) "DYLD_LIBRARY_PATH" else "LD_LIBRARY_PATH"
        val prefixDir = xtras.prefixDir(target.konanTarget)
        runTask?.environment(ldLibraryPathKey, prefixDir.resolve("lib"))
      }
    }
  }

  tasks.withType(KotlinNativeTest::class.java) {
    val hostTarget = if (HostManager.hostIsMac) {
      KonanTarget.MACOS_X64
    } else KonanTarget.LINUX_X64
    val prefixDir = xtras.prefixDir(KonanTarget.LINUX_X64)
    val ldLibraryPathKey = if (HostManager.hostIsMac) "DYLD_LIBRARY_PATH" else "LD_LIBRARY_PATH"
    environment(ldLibraryPathKey, prefixDir.resolve("lib"))
  }

}

