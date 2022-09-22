
import java.util.*

plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
}

val props = Properties().apply {
  file("../versions.properties").inputStream().use { load(it) }
}

val kotlinVersion: String = props.getProperty("version.kotlin")

kotlinDslPluginOptions {
  jvmTarget.set(provider { java.targetCompatibility.toString() })
}

kotlin {

  jvmToolchain {
    check(this is JavaToolchainSpec)
    languageVersion.set(JavaLanguageVersion.of(11))
  }


  sourceSets.all {
    languageSettings {
      listOf(
        "kotlin.RequiresOptIn",
        "kotlin.ExperimentalStdlibApi",
        "kotlin.io.path.ExperimentalPathApi",
      ).forEach {
        optIn(it)
      }
    }
  }


}


dependencies {
  implementation(gradleApi())
  implementation(gradleKotlinDsl())
  implementation(kotlin("gradle-plugin", kotlinVersion))
  //implementation(kotlin("serialization"))

}


