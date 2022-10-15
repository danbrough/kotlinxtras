
plugins {
  kotlin("jvm") version "1.7.20"
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  id("org.jetbrains.dokka") version "1.7.20"
}

repositories {
  mavenCentral()
}

kotlin {
  dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly(kotlin("gradle-plugin"))
  }
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile::class).all {

  kotlinOptions {
    jvmTarget = "11"
  }
}

tasks.withType(JavaCompile::class) {
  sourceCompatibility = JavaVersion.VERSION_11.toString()
  targetCompatibility = JavaVersion.VERSION_11.toString()
}


val projectGroup = "org.danbrough.kotlinxtras"

gradlePlugin {
  plugins {

    create("propertiesPlugin") {
      id = "$projectGroup.properties"
      implementationClass = "$projectGroup.PropertiesPlugin"
      displayName = "KotlinXtras properties plugin"
      description = "Provides a gradle properties abstraction. (For internal use)"
    }

    create("binariesConsumer") {
      id = "$projectGroup.consumer"
      implementationClass = "$projectGroup.binaries.BinariesConsumerPlugin"
      displayName = "KotlinXtras binaries consumer plugin"
      description = "Precompiled binaries for openssl,curl for compiling against."
    }

    create("binariesProvider") {
      id = "$projectGroup.provider"
      implementationClass = "$projectGroup.binaries.BinariesProviderPlugin"
      displayName = "KotlinXtras binaries provider plugin"
      description = "Add support for packaging binaries into maven publications"
    }

    create("sonatypePlugin") {
      id = "$projectGroup.sonatype"
      implementationClass = "$projectGroup.sonatype.SonatypePlugin"
      displayName = "Sonatype plugin"
      description = "Sonatype publishing support"
    }
  }
}


tasks.dokkaHtml.configure {
  outputDirectory.set(buildDir.resolve("dokka"))
//  finalizedBy("copyDocs")
}

val javadocJar by tasks.registering(Jar::class) {
  archiveClassifier.set("javadoc")
  from(tasks.dokkaHtml)
}

val sourcesJar by tasks.registering(Jar::class) {
  archiveClassifier.set("sources")
  from(sourceSets["main"].allJava)
}

publishing {

  publications.all {
    if (this !is MavenPublication) return@all
    if (project.hasProperty("publishDocs"))
      artifact(javadocJar)
    artifact(sourcesJar)
  }
}

