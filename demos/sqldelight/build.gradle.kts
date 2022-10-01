import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
  kotlin("multiplatform")

  id("app.cash.sqldelight")
}

repositories {
  maven("/usr/local/kotlinxtras/build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  maven("https://www.jetbrains.com/intellij-repository/releases")
  maven("https://cache-redirector.jetbrains.com/intellij-dependencies")

  google()
  mavenCentral()
}

kotlin {

  linuxX64()
  //linuxArm64()

  val commonMain by sourceSets.getting {
    dependencies {
      //implementation(libs.org.danbrough.sqldelight.core)
      implementation("org.danbrough.kotlinxtras:sqlite:0.0.1-beta04")
      implementation("org.danbrough.sqldelight:primitive-adapters:2.0.0-alpha03")
      implementation("org.danbrough.sqldelight:runtime:2.0.0-alpha03")
    }
  }
  val commonTest by sourceSets.getting {
    dependencies {
      implementation(kotlin("test"))
      implementation(libs.klog)
    }
  }

  val nativeMain by sourceSets.creating {
    dependencies {
      dependsOn(commonMain)
      implementation(libs.native.driver)
    }
  }

  val nativeTest by sourceSets.creating {
    dependencies {
      dependsOn(nativeMain)
    }
  }

  targets.withType(KotlinNativeTarget::class) {
    compilations["main"].apply {
      defaultSourceSet.dependsOn(nativeMain)
    }

    compilations["test"].apply {
      defaultSourceSet.dependsOn(nativeTest)
    }
  }

}



sqldelight {
  database("Database") {
    packageName = "demo"
  }
}