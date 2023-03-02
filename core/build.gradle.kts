plugins {
  `kotlin-dsl`
  `maven-publish`
  id("org.jetbrains.dokka")
  xtras("sonatype", Xtras.version)
}

dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
  compileOnly("org.danbrough.kotlinxtras:plugin:${Xtras.version}")
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

