import org.danbrough.kotlinxtras.enableCurl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  xtras("core",Xtras.version)
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
      implementation(project(":common"))
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
      }
    }
  }
}

