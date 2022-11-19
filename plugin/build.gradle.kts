plugins {
  `kotlin-dsl`
  //`java-gradle-plugin`
  `maven-publish`
  id("org.jetbrains.dokka") version "1.7.20"

  id("org.danbrough.kotlinxtras.sonatype") version "0.0.1-beta01"
}

repositories {
  mavenCentral()
}


sonatype{
  localRepoLocation = project.file("../build/m2")
}


group = "org.danbrough.kotlinxtras"
version = "0.0.1-beta02"

dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
  compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")
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

gradlePlugin {
  plugins {
    create("xtrasPlugin") {
      id = "${group}.xtras"
      implementationClass = "$group.XtrasPlugin"
      displayName = "Xtras Plugin"
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

/*
publishing {
  repositories {
    maven(file("../build/m2")) {
      name = "M2"
    }
  }
}

*/
