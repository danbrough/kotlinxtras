import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  kotlin("multiplatform")
}


repositories {
  maven("../../build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()
}


kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
  androidNativeX86()
  androidNativeX64()
  androidNativeArm32()
  androidNativeArm64()
  macosArm64()
  macosX64()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.curl)
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
        runTask?.apply {
          properties["url"]?.also {
            args(it.toString())
          }

          environment("CA_CERT_FILE",file("cacert.pem"))
          environment(if (HostManager.hostIsMac) "DYLD_LIBRARY_PATH" else "LD_LIBRARY_PATH",
            "/usr/local/kotlinxtras/libs/openssl/${konanTarget.platformName}/lib${File.pathSeparatorChar}\"/usr/local/kotlinxtras/libs/curl/${konanTarget.platformName}/lib\"")

        }

      }
    }
  }




}




val org.jetbrains.kotlin.konan.target.KonanTarget.platformName: String
  get() {
    if (family == org.jetbrains.kotlin.konan.target.Family.ANDROID) {
      return when (this) {
        org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_X64 -> "androidNativeX64"
        org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_X86 -> "androidNativeX86"
        org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_ARM64 -> "androidNativeArm64"
        org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_ARM32 -> "androidNativeArm32"
        else -> throw Error("Unhandled android target $this")
      }
    }
    return name.split("_").joinToString("") { it.capitalize() }.decapitalize()
  }





