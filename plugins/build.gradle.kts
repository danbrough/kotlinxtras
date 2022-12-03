plugins {
  `kotlin-dsl`
  `maven-publish`
  id("org.jetbrains.dokka")
  id("${Xtras.projectGroup}.sonatype")
  //id("${Xtras.projectGroup}.binaries")
  xtras("binaries")
}



repositories {
  mavenCentral()
}

dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
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



