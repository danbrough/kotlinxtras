import org.danbrough.xtras.SHARED_LIBRARY_PATH_NAME
import org.danbrough.xtras.declareHostTarget
import org.danbrough.xtras.mqtt.xtrasMQTT
import org.danbrough.xtras.openssl.xtrasOpenSSL
import org.danbrough.xtras.platformName
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.xtras.openssl)
  alias(libs.plugins.xtras.mqtt)
}


val ssl = xtrasOpenSSL()

val mqtt = xtrasMQTT(ssl) {

  cinterops {
    headersSourceCode = null
    headersSourceFile = file("headers.h")
  }

}

kotlin {

  declareHostTarget()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.org.danbrough.klog)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  val execBuildTypes = setOf(NativeBuildType.DEBUG)

  targets.withType<KotlinNativeTarget> {
    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }
    binaries {
      executable("subscribe", buildTypes = execBuildTypes) {
        entryPoint = "demo.subscribe.main"

        linkTask.apply {
          println("LINKEROPTS: $linkerOpts")

        }
        runTask?.apply {

          environment.apply {
            keys.forEach {
              println("ENV: $it -> ${this[it]}")
            }
            this[SHARED_LIBRARY_PATH_NAME] =
              mqtt.libsDir(target.konanTarget).resolve("lib").absolutePath
          }
        }
      }
    }
  }
}


tasks.create("runSubscribe") {
  dependsOn("runSubscribeDebugExecutable${HostManager.host.platformName}")
}


