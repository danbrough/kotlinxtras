import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  `kotlin-dsl`
}

dependencies {
 implementation(kotlin("gradle-plugin",libs.versions.kotlin.get()))
}

repositories {
  mavenCentral()
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