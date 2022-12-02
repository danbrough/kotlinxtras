import Pommy_gradle.Pommy.xtrasPom

plugins {
  //kotlin("jvm")
  `kotlin-dsl`
  //`java-gradle-plugin`
  `maven-publish`
  `signing`
  id("org.jetbrains.dokka")
}

group = "org.danbrough.kotlinxtras"
version = "0.0.3-beta02"

repositories {
  mavenCentral()
}


dependencies {
  add("compileOnly", kotlin("gradle-plugin"))
  add("compileOnly", kotlin("gradle-plugin-api"))
}

//kotlin {
//  dependencies {
//    compileOnly(gradleApi())
//    compileOnly(gradleKotlinDsl())
//    compileOnly(kotlin("gradle-plugin"))
//  }
//}


tasks.withType(JavaCompile::class) {
  sourceCompatibility = JavaVersion.VERSION_11.toString()
  targetCompatibility = JavaVersion.VERSION_11.toString()
}



gradlePlugin {
  plugins {

    create("propertiesPlugin") {
      id = "$group.properties"
      implementationClass = "$group.PropertiesPlugin"
      displayName = "KotlinXtras properties plugin"
      description = "Provides a gradle properties abstraction. (For internal use)"
    }

    create("binariesConsumer") {
      id = "$group.consumer"
      implementationClass = "$group.binaries.BinariesConsumerPlugin"
      displayName = "KotlinXtras binaries consumer plugin"
      description = "Precompiled binaries for openssl,curl for compiling against."
    }

    create("binariesProvider") {
      id = "$group.provider"
      implementationClass = "$group.binaries.BinariesProviderPlugin"
      displayName = "KotlinXtras binaries provider plugin"
      description = "Add support for packaging binaries into maven publications"
    }

    create("sonatypePlugin") {
      id = "$group.sonatype"
      implementationClass = "$group.sonatype.SonatypePlugin"
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
    group = project.group
    version = project.version
    if (this !is MavenPublication) return@all
    if (project.hasProperty("publishDocs"))
      artifact(javadocJar)
    artifact(sourcesJar)
    if (hasProperty("signPublications"))
      signing.sign(this)
  }

  repositories {
    maven("../build/m2"){
      name = "M2"
    }

    maven{
      name = "SonaType"
      url = uri("https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/${System.getenv("SONATYPE_REPO_ID")}")
      credentials{
        username = property("sonatypeUsername")?.toString()
        password = property("sonatypePassword")?.toString()
      }
    }
  }

  publications.all {
    if (this !is MavenPublication) return@all


    xtrasPom()
  }


}


