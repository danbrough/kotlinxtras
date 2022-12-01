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

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
  kotlinOptions {
    jvmTarget = "11"
  }
}