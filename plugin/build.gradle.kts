
plugins {
  `kotlin-dsl`
  `maven-publish`
}


val javaLangVersion = 8

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(javaLangVersion))
  /*sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8*/
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(javaLangVersion))
  }
}


group = libs.versions.xtrasPackage.get()
version = libs.versions.xtrasPublishing.get()

repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven") {
    name = "Xtras"
  }
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()
}

publishing {
  repositories {
    maven("/usr/local/kotlinxtras/build/xtras/maven") {
      name = "Xtras"
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


  /*
        project.extensions.findByType<KotlinProjectExtension>()?.apply {
        sourceSets.findByName("main")?.kotlin?.also { srcDir ->
          val sourcesJarTask = tasks.register("sourcesJar${name.capitalize()}", Jar::class.java) {
            archiveClassifier.set("sources")
            from(srcDir)
          }

          publications.all {
            if (this is MavenPublication)
              artifact(sourcesJarTask)
          }
        }
   */
}

dependencies {
  //add("compileOnly", kotlin("gradle-plugin"))
  //add("compileOnly", kotlin("gradle-plugin-api"))
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

