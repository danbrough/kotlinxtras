import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(kotlin("gradle-plugin","1.7.22"))

}

repositories {
  mavenCentral()
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(11))
  //sourceCompatibility = JavaVersion.VERSION_11
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



