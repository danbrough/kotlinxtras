plugins {
  `kotlin-dsl`
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


version = libs.versions.kotlinXtrasPublishing.get()

dependencies {
  //add("compileOnly", kotlin("gradle-plugin"))
  //add("compileOnly", kotlin("gradle-plugin-api"))
  implementation(libs.org.danbrough.klog)
  api(libs.kotlin.gradle.plugin)
}

gradlePlugin {
  plugins {
    create("xtras") {
      id = "$group.xtras"
      implementationClass = "$group.XtrasPlugin"
    }
  }
}

