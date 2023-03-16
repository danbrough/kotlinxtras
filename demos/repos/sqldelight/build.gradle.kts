import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.danbrough.kotlinxtras.core.enableSqlite

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.core")
  id("org.danbrough.sqldelight")
}


enableSqlite {}



repositories {
  maven("/usr/local/kotlinxtras/build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  maven("https://www.jetbrains.com/intellij-repository/releases")
  maven("https://cache-redirector.jetbrains.com/intellij-dependencies")

  google()
  mavenCentral()
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
      implementation("org.danbrough.kotlinxtras:sqlite:_")
      implementation("org.danbrough.sqldelight:primitive-adapters:_")
      implementation("org.danbrough.sqldelight:runtime:_")
    }
  }


  val nativeMain by sourceSets.creating {
    dependencies {
      dependsOn(commonMain)
      implementation("org.danbrough.sqldelight:native-driver:_")
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

