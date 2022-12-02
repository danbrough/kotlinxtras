plugins {
  `kotlin-dsl`
  `maven-publish`
  alias(libs.plugins.org.jetbrains.dokka)
  alias(libs.plugins.org.danbrough.kotlinxtras.sonatype)
  alias(libs.plugins.org.danbrough.kotlinxtras.binaries)
}


repositories {
  mavenCentral()
}

dependencies {

  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
  compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:${libs.versions.dokka.get()}")
  compileOnly(project(":plugin"))

}


gradlePlugin {
  plugins {


    create("curl") {
      id = "$group.curl"
      implementationClass = "$group.CurlPlugin"
      displayName = "KotlinXtras curl plugin"
      description = "Provides curl support to multi-platform projects"
    }

    create("iconv") {
      id = "$group.iconv"
      implementationClass = "$group.IconvPlugin"
      displayName = "KotlinXtras iconv plugin"
      description = "Provides iconv support to multi-platform projects"
    }

    create("openssl") {
      id = "$group.openssl"
      implementationClass = "$group.OpenSSLPlugin"
      displayName = "KotlinXtras openssl plugin"
      description = "Provides openssl support to multi-platform projects"
    }

    create("sqlite") {
      id = "$group.sqlite"
      implementationClass = "$group.SqlitePlugin"
      displayName = "KotlinXtras sqlite plugin"
      description = "Provides sqlite support to multi-platform projects"
    }

  }
}



