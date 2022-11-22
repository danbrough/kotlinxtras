
plugins {
  `kotlin-dsl`
  `maven-publish`
  `signing`
  id("org.jetbrains.dokka")
  id("org.danbrough.kotlinxtras.xtras")
  id("org.danbrough.kotlinxtras.sonatype")
}

group = "org.danbrough.kotlinxtras"


dependencies {
  compileOnly( kotlin("gradle-plugin"))
  compileOnly( kotlin("gradle-plugin-api"))
  implementation(libs.xtras)
}

sonatype {
  configurePublishing {
    it.afterEvaluate {
      publications.all {
        this as MavenPublication
        println("PUBLICATION:  name:$name : version:$version artifactID:${artifactId} artifacts:${artifacts}")
        if (artifactId == "curl" || artifactId == "$group.curl.gradle.plugin")
          version = "7_86_0a"
      }
    }
  }
}

gradlePlugin {
  plugins {
    create("curl") {
      id = "$group.curl"
      implementationClass = "$group.CurlPlugin"
      displayName = "KotlinXtras curl plugin"
      description = "Provides curl support to multi-platform projects"
    }
  }
}

