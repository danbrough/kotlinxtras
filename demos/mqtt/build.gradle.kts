import org.danbrough.xtras.declareHostTarget
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.Family
import java.net.URL

plugins {
  kotlin("multiplatform")
}

buildscript {
  dependencies {
    classpath(libs.mqtt.plugin)
  }
}


repositories {
  maven(extra["xtrasRepo"].toString())
  mavenCentral()
}

kotlin {
  declareHostTarget()
  jvm()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.org.danbrough.klog)
    }
  }
  val nativeMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget> {
    compilations["main"].apply {

      dependencies {
        implementation(libs.mqtt.lib)
      }

      defaultSourceSet.dependsOn(nativeMain)
    }

    binaries {
      executable("demo") {
        entryPoint("demo.mqtt.main")
      }
    }
  }

}