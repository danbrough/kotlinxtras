import org.danbrough.xtras.curl.xtrasCurl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.xtras.openssl)
  alias(libs.plugins.xtras.curl)
}

val openSSLDir = rootDir.resolve("xtras/libs/openSSL/3.1.3")
val curlDIR = rootDir.resolve("xtras/libs/curl/8.3.0")

/*
val ssl = xtrasOpenSSL {

}

xtrasCurl(ssl) {
}
*/

/*

dependencies {
  project(":binaries")
}
*/

kotlin {
  //linuxX64()
  mingwX64()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.org.danbrough.klog)
    }
  }

  val commonTest by sourceSets.getting {
    dependencies {
      implementation(kotlin("test"))
    }
  }

  val nativeTest by sourceSets.creating {
    dependsOn(commonTest)
  }

  val nativeMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget> {
    println("CONFIGURING TARGET: ${this.konanTarget}")
    compilations["main"].apply {
      defaultSourceSet.dependsOn(nativeMain)
      cinterops {
        create("curl") {
          packageName = "libcurl"
          defFile = file("curl.def")
        }
      }
    }
    compilations["test"].apply {
      defaultSourceSet.dependsOn(nativeTest)
    }



    binaries {
      executable("demo", buildTypes = setOf(NativeBuildType.DEBUG)) {
        entryPoint = "demo.main"
        runTask?.environment("CA_CERT_FILE", File("C:/xtras/test/cacert.pem"))
        findProperty("args")?.also {
          runTask?.args(it.toString())
        }
      }
    }
  }
}

