plugins {
  `kotlin-dsl`
  `maven-publish`
  id("org.jetbrains.dokka")
  xtras("sonatype")
}

dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
  api(project(":plugin"))
}

gradlePlugin {
  plugins {
    create("core") {
      id = "$group.core"
      implementationClass = "$group.CorePlugin"
      displayName = "KotlinXtras core plugins"
      description = "Provides some core plugins"
    }
  }
}


