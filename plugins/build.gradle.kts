plugins {
  `kotlin-dsl`
  `maven-publish`
  `signing`
  id("org.jetbrains.dokka")
  id("org.danbrough.kotlinxtras.xtras")
  id("org.danbrough.kotlinxtras.sonatype")
}

group = "org.danbrough.kotlinxtras"
version = libs.versions.xtras.get()

dependencies {
  compileOnly( kotlin("gradle-plugin"))
  compileOnly( kotlin("gradle-plugin-api"))
  implementation(libs.xtras)
}

sonatype {
  configurePublishing {
    it.afterEvaluate {
      publications.all {
        if (this !is MavenPublication) return@all

        if (artifactId == "plugins"){
          version = project.version.toString()
          return@all
        }

        val id = if (artifactId.endsWith(".gradle.plugin"))
          artifactId.substringAfter("${group}.").substringBefore(".gradle.plugin")
        else artifactId

        findProperty("version.$id")?.toString()?.also {pluginVersion->
          version = pluginVersion
        } ?: throw Error("No version for artifact: $artifactId")

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

  plugins {
    create("iconv") {
      id = "$group.iconv"
      implementationClass = "$group.IconvPlugin"
      displayName = "KotlinXtras iconv plugin"
      description = "Provides iconv support to multi-platform projects"
    }
  }

  plugins {
    create("openssl") {
      id = "$group.openssl"
      implementationClass = "$group.OpenSSLPlugin"
      displayName = "KotlinXtras openssl plugin"
      description = "Provides openssl support to multi-platform projects"
    }
  }
}

