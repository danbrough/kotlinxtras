import org.danbrough.kotlinxtras.curl.xtrasCurl
import org.danbrough.kotlinxtras.declareHostTarget
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.wolfssl.xtrasWolfSSL
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras.wolfssl)
  alias(libs.plugins.kotlinXtras.curl)
}


val ssl = xtrasWolfSSL()

xtrasCurl(ssl) {
  println("xtrasCurl.supportedTargets = $supportedTargets version:$sourceConfig")
  cinterops {
    headers = """
          headers = curl/curl.h
          linkerOpts =  -lz -lssl -lcrypto -lcurl
          #staticLibraries.linux = libcurl.a
          #staticLibraries.android = libcurl.a
          
          """.trimIndent()
  }
}

kotlin {
  jvm()
  declareHostTarget()

  if (HostManager.hostIsLinux) {
    linuxArm64()
  }

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.org.danbrough.klog)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget> {
    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
/*      cinterops{
        create("curl"){
          defFile = project.rootProject.rootDir.resolve("build/xtras/cinterops/xtras_curl.def")
        }
      }*/
    }
    binaries {
      executable("demo1") {
        entryPoint = "demo.demo1.main"
      }
    }
  }
}


tasks.create("runDemo1") {
  dependsOn("runDemo1DebugExecutable${HostManager.host.platformName}")
}


