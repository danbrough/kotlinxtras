plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
}


kotlinDslPluginOptions {
  jvmTarget.set(provider { java.targetCompatibility.toString() })
}

kotlin {

  jvmToolchain {
    check(this is JavaToolchainSpec)
    languageVersion.set(JavaLanguageVersion.of(11))
  }



}


