import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(kotlin("gradle-plugin"))

}

repositories {
  mavenCentral()
}


val javaLangVersion = 11

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(javaLangVersion))
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(javaLangVersion))
  }
}
