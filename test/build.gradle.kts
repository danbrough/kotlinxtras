import org.danbrough.xtras.curl.xtrasCurl
import org.danbrough.xtras.wolfssl.xtrasWolfSSL
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.xtras.wolfssl)
  alias(libs.plugins.xtras.curl)
}

val ssl = xtrasWolfSSL{

}

xtrasCurl(ssl){
}


kotlin {
  linuxX64()
  mingwX64()
  linuxArm64()


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
    compilations["main"].apply {
      defaultSourceSet.dependsOn(nativeMain)
    }
    compilations["test"].apply {
      defaultSourceSet.dependsOn(nativeTest)
    }

    this.binaries {
      executable("demo", buildTypes = setOf(NativeBuildType.DEBUG)) {
        entryPoint = "demo.main"
        runTask?.environment("CA_CERT_FILE", file("cacert.pem"))
        findProperty("args")?.also {
          runTask?.args(it.toString())
        }
      }
    }
  }
}

