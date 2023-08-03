plugins {
  `kotlin-dsl`
}

dependencies {
  compileOnly(kotlin("gradle-plugin"))
}

repositories {
  mavenCentral()
}


val javaLangVersion = 8

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(javaLangVersion))
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(javaLangVersion))
  }
}
