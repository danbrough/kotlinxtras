plugins {
  `kotlin-dsl`
  `java-gradle-plugin`


  `maven-publish`
  id("org.danbrough.kotlinxtras.sonatype") version "0.0.1"
}

repositories {
  mavenCentral()
}

sonatype{
}
group = "org.danbrough.kotlinxtras"
version = "0.0.1"

dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
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

println("REPOID: ${project.findProperty("sonatypeRepositoryId")}")
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

publishing {
  repositories {
    maven(file("../build/m2")) {
      name = "M2"
    }
  }
}


