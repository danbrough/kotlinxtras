import java.util.Locale

plugins {
  `kotlin-dsl`
  `maven-publish`
}


val javaLangVersion = 8

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(javaLangVersion))
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(javaLangVersion))
  }
}

group = libs.versions.xtrasPackage.get()
version = libs.versions.xtrasPublishing.get()

repositories {
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()
}


val xtrasMavenDir = if (hasProperty("xtras.dir.maven"))
  File(property("xtras.dir.maven").toString())
else if (hasProperty("xtras.dir"))
  File(property("xtras.dir").toString()).resolve("maven")
else error("Neither xtras.dir.maven or xtras.dir are set")

publishing {

  repositories {
    maven(xtrasMavenDir) {
      name = "xtras"
    }
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
  }

  kotlin.sourceSets.findByName("main")?.kotlin?.also { srcDir ->
    val sourcesJarTask = tasks.register<Jar>(
      "sourcesJar${
        name.replaceFirstChar {
          if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
          ) else it.toString()
        }
      }"
    ) {
      archiveClassifier.set("sources")
      from(srcDir)
    }
    publications.all {
      if (this is MavenPublication)
        artifact(sourcesJarTask)
    }
  }

}

dependencies {
  compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
  plugins {
    create("xtras") {
      id = group.toString()
      implementationClass = "$group.XtrasPlugin"
    }
  }
}

gradlePlugin {
  plugins {
    create("settings") {
      id = "$group.settings"
      implementationClass = "$group.XtrasSettingsPlugin"
    }
  }
}

