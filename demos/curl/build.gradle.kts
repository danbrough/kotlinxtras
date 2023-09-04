import org.danbrough.kotlinxtras.curl.xtrasCurl
import org.danbrough.kotlinxtras.declareHostTarget
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.runningInIDE
import org.danbrough.kotlinxtras.tasks.konanDepsTaskName
import org.danbrough.kotlinxtras.openssl.xtrasOpenSSL
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras.openssl)
  alias(libs.plugins.kotlinXtras.curl)
}


val ssl = xtrasOpenSSL()

xtrasCurl(ssl) {
  println("xtrasCurl.supportedTargets = $supportedTargets version:$sourceConfig")
}

kotlin {
  jvm()
  declareHostTarget()

  if (HostManager.hostIsLinux && !project.runningInIDE) {
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
        runTask?.environment("CA_CERT_FILE", file("cacert.pem"))
        findProperty("args")?.also {
          runTask?.args(it.toString())
        }
      }
    }
  }
}


tasks.create("runDemo1") {
  dependsOn("runDemo1DebugExecutable${HostManager.host.platformName}")
}


tasks.create("test"){
  dependsOn(KonanTarget.LINUX_ARM64.konanDepsTaskName)
  doLast {
    println("FINISHED TEST")
  }
}