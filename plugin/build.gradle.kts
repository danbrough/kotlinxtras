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

publishing {
  repositories {

    maven(property("xtras.dir.maven")?.toString() ?: file("../maven")) {
      name = "xtras"
    }
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
  }

  kotlin.sourceSets.findByName("main")?.kotlin?.also { srcDir ->
    val sourcesJarTask = tasks.register<Jar>("sourcesJar${name.capitalize()}") {
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
  implementation(libs.org.danbrough.klog)
  api(libs.kotlin.gradle.plugin)
}

gradlePlugin {
  plugins {
    create("xtras") {
      id = group.toString()
      implementationClass = "$group.XtrasPlugin"
    }
  }
}

