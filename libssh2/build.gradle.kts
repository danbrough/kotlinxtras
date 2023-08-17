import org.danbrough.kotlinxtras.core.enableLibSSH2
import org.danbrough.kotlinxtras.core.enableWolfSSL
import org.danbrough.kotlinxtras.declareSupportedTargets
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinxtras.sonatype)
  alias(libs.plugins.kotlinxtras.core)
}

version = "0.0.1-beta01"
val ssl = enableWolfSSL {

}

enableLibSSH2(ssl) {
  deferToPrebuiltPackages = true

  cinterops {
    headers = """
          headers = wolfssl/ssl.h
          linkerOpts =  -lz -lssl -lcrypto -lcurl
          #staticLibraries.linux = libcurl.a
          #staticLibraries.android = libcurl.a
          
          """.trimIndent()
  }
}

kotlin {

  //declareSupportedTargets()
  jvm()
  linuxX64()

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.klog)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }

  val posixMain by sourceSets.creating {

  }

  targets.withType<KotlinNativeTarget> {
    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }
  }


}


