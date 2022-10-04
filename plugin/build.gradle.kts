
plugins {
  kotlin("jvm")
  `java-gradle-plugin`
  `maven-publish`
  id("org.jetbrains.dokka")
}


kotlin {
  dependencies {
    implementation(gradleKotlinDsl())
    implementation(kotlin("gradle-plugin"))
  }
}



gradlePlugin {
  plugins {

    create("xtrasPlugin") {
      id = "org.danbrough.kotlinxtras.xtras"
      implementationClass = "org.danbrough.kotlinxtras.XtrasPlugin"
      displayName = "KotlinXtras plugin"
      description = "Cross compiled binaries for openssl,curl"
    }

    create("propertiesPlugin") {
      id = "org.danbrough.kotlinxtras.properties"
      implementationClass = "org.danbrough.kotlinxtras.PropertiesPlugin"
      displayName = "KotlinXtras properties plugin"
      description = "Provides a gradle properties abstraction. (For internal use)"
    }


    create("binaries") {
      id = "org.danbrough.kotlinxtras.binaries"
      implementationClass = "org.danbrough.kotlinxtras.binaries.BinariesPlugin"
      displayName = "KotlinXtras binaries plugin"
      description = "Precompiled binaries for openssl,curl for compiling against."
    }

    create("binariesProvider") {
      id = "org.danbrough.kotlinxtras.binaries.provider"
      implementationClass = "org.danbrough.kotlinxtras.binaries.BinariesProviderPlugin"
      displayName = "KotlinXtras binaries provider plugin"
      description = "Add support for packaging binaries into maven publications"
    }

    create("sonatypePlugin") {
      id = "org.danbrough.kotlinxtras.sonatype"
      implementationClass = "org.danbrough.kotlinxtras.sonatype.SonatypePlugin"
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

