import org.danbrough.kotlinxtras.core.enableLibSSH2
import org.danbrough.kotlinxtras.core.enableOpenssl3
import org.danbrough.kotlinxtras.declareSupportedTargets
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinxtras.sonatype)
  alias(libs.plugins.kotlinxtras.core)
}

version = "0.0.1-beta01"

enableLibSSH2(enableOpenssl3()) {
  deferToPrebuiltPackages = true

  cinterops {
    interopsPackage = "$group.${project.name}"
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


