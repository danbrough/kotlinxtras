import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.danbrough.kotlinxtras.binaries.LibraryExtension
import org.danbrough.kotlinxtras.binaries.git
import org.danbrough.kotlinxtras.binaries.registerLibraryExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  `kotlin-dsl`
  id("org.danbrough.kotlinxtras.binaries") version "0.0.3-beta12"

}

dependencies {
  implementation(kotlin("gradle-plugin","1.7.10"))
  implementation("org.danbrough.kotlinxtras.binaries:org.danbrough.kotlinxtras.binaries.gradle.plugin:0.0.3-beta12")

}

repositories {
  mavenCentral()
  maven(file("../../../build/m2"))
}

kotlin {
  jvmToolchain {
    check(this is JavaToolchainSpec)
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

tasks.withType<KotlinJvmCompile> {
  kotlinOptions {
    jvmTarget = "11"
  }
}
gradlePlugin {
  plugins {
    create("zlib") {
      id = "demo.zlib"
      implementationClass = "ZLibPlugin"
      displayName = "KotlinXtras zlib plugin"
      description = "Provides zlib support to multi-platform projects"
    }

  }
}