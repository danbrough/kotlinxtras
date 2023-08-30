plugins {
  `kotlin-dsl`
  `maven-publish`

  alias(libs.plugins.org.jetbrains.dokka)
  alias(libs.plugins.kotlinxtras.sonatype)
}

dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
  //implementation(libs.plugin)
  compileOnly(libs.kotlinxtras.plugin)

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

