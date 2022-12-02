import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.util.*

plugins {
  `kotlin-dsl`
}

val kotlinVersion = Properties().let{
  it.load(file("../versions.properties").reader())
  it["version.kotlin"]!!.toString()
}

dependencies {
 implementation(kotlin("gradle-plugin",kotlinVersion))
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