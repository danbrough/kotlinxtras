plugins {
  `kotlin-dsl`
  `maven-publish`
  id("org.jetbrains.dokka")
  xtras("sonatype")
}

repositories {
  mavenCentral()
}


dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
  compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:_")

}

gradlePlugin {
  plugins {

    create("binariesPlugin") {
      id = "${group}.binaries"
      implementationClass = "$group.binaries.BinaryPlugin"
      displayName = "Xtras Binaries Plugin"
      description = "Provides native library support to Kotlin applications"
    }

    create("sonatypePlugin") {
      id = "$group.sonatype"
      implementationClass = "$group.sonatype.SonatypePlugin"
      displayName = "Sonatype plugin"
      description = "Sonatype publishing support"
    }

  }
}



kotlin {
  jvmToolchain {
    check(this is JavaToolchainSpec)
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile> {
  kotlinOptions {
    jvmTarget = "11"
  }
}

