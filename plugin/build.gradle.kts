plugins {
  `kotlin-dsl`
  `maven-publish`
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


group = libs.versions.xtrasPackage.get()
version = libs.versions.xtrasPublishing.get()

repositories {
  maven(rootProject.layout.buildDirectory.dir("xtras/maven"))
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()
}

dependencies {
  //add("compileOnly", kotlin("gradle-plugin"))
  //add("compileOnly", kotlin("gradle-plugin-api"))
  implementation(libs.org.danbrough.klog)
  api(libs.kotlin.gradle.plugin)
}

gradlePlugin {
  plugins {
    create("xtras") {
      id = group.toString()
      implementationClass = "$group.XtrasPlugin"
    }
  }
}

