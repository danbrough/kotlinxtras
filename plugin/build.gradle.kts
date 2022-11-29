import org.danbrough.kotlinxtras.xtrasPom

plugins {
  `kotlin-dsl`
  `maven-publish`
  alias(libs.plugins.org.jetbrains.dokka)
  alias(libs.plugins.org.danbrough.kotlinxtras.sonatype)
}

repositories {
  mavenCentral()
}


group = "org.danbrough.kotlinxtras"
version = "0.0.3-beta02"

dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
  compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:${libs.versions.dokka.get()}")
}

sonatype {
  localRepoLocation = project.file("../build/m2")

  configurePublishing {
    publications.all {
      if (this is MavenPublication)
        xtrasPom()
    }
  }
}

kotlin {
  jvmToolchain {
    check(this is JavaToolchainSpec)
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
  kotlinOptions {
    jvmTarget = "11"
  }
}

gradlePlugin {
  plugins {

    create("binariesPlugin") {
      id = "${group}.binaries"
      implementationClass = "$group.binaries.BinaryPlugin"
      displayName = "Xtras Binaries Plugin"
      description = "Provides native library support to Kotlin applications"
    }

    create("sonatypePlugin") {
      id = "$group.sonatype"
      implementationClass = "$group.sonatype.SonatypePlugin"
      displayName = "Sonatype plugin"
      description = "Sonatype publishing support"
    }

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



