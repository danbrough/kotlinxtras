import org.danbrough.kotlinxtras.declareSupportedTargets
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras)

}

repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
  google()
}

publishing {
  repositories {
    maven("/usr/local/kotlinxtras/build/xtras/maven") {
      name = "xtras"
    }
  }
}

kotlin {
  declareSupportedTargets()

  targets.withType<KotlinNativeTarget> {
    binaries {
      executable("demo") {
        entryPoint = "demo.main"
      }
    }
  }
}
