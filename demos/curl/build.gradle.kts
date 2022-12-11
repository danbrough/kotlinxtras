import org.danbrough.kotlinxtras.enableCurl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.core")

}


repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}


enableCurl {
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

  val posixMain by sourceSets.creating{
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget> {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }


    binaries {
      executable("curlDemo") {
        entryPoint = "demo1.main"
        runTask?.environment("CA_CERT_FILE",file("cacert.pem"))
        findProperty("args")?.also {
          runTask?.args(it.toString())
        }
      }
    }
  }
}



tasks.create("run"){
  dependsOn("runCurlDemoDebugExecutable${if (HostManager.hostIsMac) "MacosX64" else "LinuxX64"}")
}
