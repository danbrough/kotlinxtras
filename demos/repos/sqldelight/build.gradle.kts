import org.danbrough.kotlinxtras.binaries.CurrentVersions.enableSqlite
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.consumer")
  id("org.danbrough.sqldelight")
}

repositories {
  maven("/usr/local/kotlinxtras/build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  maven("https://www.jetbrains.com/intellij-repository/releases")
  maven("https://cache-redirector.jetbrains.com/intellij-dependencies")

  google()
  mavenCentral()
}

binaries {
  enableSqlite()
}


sqldelight {
  database("Database") {
    packageName = "demo"
  }
}

kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
  androidNativeX86()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.sqlite)
      implementation(libs.primitive.adapters)
      implementation(libs.org.danbrough.sqldelight.runtime)
    }
  }


  val nativeMain by sourceSets.creating {
    dependencies {
      dependsOn(commonMain)
      implementation(libs.native.driver)
    }
  }


  targets.withType(KotlinNativeTarget::class) {
    compilations["main"].apply {
      defaultSourceSet.dependsOn(nativeMain)
    }

    binaries {
      executable("demo1") {
        entryPoint("demo.main")
      }
    }
  }

}



