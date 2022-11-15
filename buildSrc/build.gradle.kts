plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(kotlin("gradle-plugin", "1.6.21"))
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
