
plugins {
  `kotlin-dsl`
  `maven-publish`
  `signing`
  id("org.jetbrains.dokka") version "1.7.20"
}

group = "org.danbrough.kotlinxtras"
version = "0.0.1-beta01"

repositories {
  mavenCentral()
  gradlePluginPortal()
}


//
//kotlin {
//  dependencies {
//    compileOnly(gradleApi())
//    compileOnly(gradleKotlinDsl())
//    compileOnly(kotlin("gradle-plugin"))
//  }
//}

dependencies {
  //add("compileOnly", gradleKotlinDsl())

  add("compileOnly", kotlin("gradle-plugin"))
  add("compileOnly", kotlin("gradle-plugin-api"))
}

gradlePlugin {
  plugins {
    create("iconvPlugin") {
      id = "$group.iconv"
      implementationClass = "$group.IconvPlugin"
      displayName = "KotlinXtras iconv plugin"
      description = "Provides iconv support to multi-platform projects"
    }
  }
}



tasks.withType(JavaCompile::class) {
  sourceCompatibility = JavaVersion.VERSION_11.toString()
  targetCompatibility = JavaVersion.VERSION_11.toString()
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


    //xtrasPom()
  }


}


