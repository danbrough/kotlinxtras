import org.danbrough.kotlinxtras.core.enableCurl
import org.danbrough.kotlinxtras.core.enableLibSSH2
import org.danbrough.kotlinxtras.core.enableOpenssl3

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras)
}

repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}

val openSSL = enableOpenssl3()

enableCurl(openSSL) {
  cinterops {
    interopsPackage = "libcurl"
  }
}

enableLibSSH2(openSSL) {
  cinterops {
    interopsPackage = "libssh2"
  }
}



kotlin {

  linuxX64()
  @Suppress("DEPRECATION")
  linuxArm32Hfp()
  linuxArm64()

  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
  }

  androidNativeX86()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.org.danbrough.kotlinxtras.common)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget> {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }

    binaries {
      executable("curlDemo") {
        entryPoint = "demo1.main"
        runTask?.environment("CA_CERT_FILE", file("cacert.pem"))
        findProperty("args")?.also {
          runTask?.args(it.toString())
        }
      }

      executable("sshDemo") {
        entryPoint = "demo2.main"
        findProperty("args")?.also {
          runTask?.args(it.toString())
        }
      }
    }
  }
}



tasks.create("runCurl") {
  dependsOn("runCurlDemoDebugExecutable${if (HostManager.hostIsMac) "MacosX64" else "LinuxX64"}")
}
