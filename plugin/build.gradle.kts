plugins {
  `kotlin-dsl`
  //`java-gradle-plugin`
  `maven-publish`
}

repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven") {
    name = "Xtras"
  }
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
  gradlePluginPortal()
  google()
}

publishing {
  repositories {
    maven("/usr/local/kotlinxtras/build/xtras/maven") {
      name = "xtras"
    }
  }
}

val javaLangVersion = 8

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(javaLangVersion))
  /*sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8*/
}
kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(javaLangVersion))
  }
}

group = "org.danbrough.kotlinxtras"
version = libs.versions.kotlinXtrasPublishing.get()



dependencies {
  add("compileOnly", kotlin("gradle-plugin"))
  add("compileOnly", kotlin("gradle-plugin-api"))
  implementation(libs.org.danbrough.klog)
}

gradlePlugin {
  plugins {
    create("xtras") {
      id = "$group.xtras"
      implementationClass = "$group.XtrasPlugin"
    }
  }
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
}
