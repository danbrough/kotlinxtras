

plugins {
  `kotlin-dsl`
  `maven-publish`
}

repositories {
  mavenCentral()
}

group = "org.danbrough.kotlinxtras"
version = "0.0.1"

dependencies {
  compileOnly( kotlin("gradle-plugin"))
  compileOnly( kotlin("gradle-plugin-api"))
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

publishing {
  repositories {
    maven(file("../build/m2")){
      name = "M2"
    }
  }
}