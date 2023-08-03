plugins {
  `kotlin-dsl`
  `maven-publish`
  id("org.jetbrains.dokka")
  xtras("sonatype", Xtras.version)
}

dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
  implementation("org.danbrough.kotlinxtras:plugin:${Xtras.version}")
}

repositories {
  maven("https://repo.maven.apache.org/maven2/")

  mavenCentral()
}

gradlePlugin {
  plugins {
    create("core") {
      id = "$group.core"
      implementationClass = "$group.core.CorePlugin"
      displayName = "KotlinXtras core plugins"
      description = "Provides some core plugins"
    }
  }
}

