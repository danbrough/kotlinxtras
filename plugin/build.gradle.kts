
plugins {
  kotlin("jvm")
  `java-gradle-plugin`
  `maven-publish`
  id("org.jetbrains.dokka")
}


kotlin {
  dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(kotlin("gradle-plugin"))
  }

/*  jvmToolchain {
    check(this is JavaToolchainSpec)
    languageVersion.set(JavaLanguageVersion.of())
  }*/
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile::class).all {

  kotlinOptions {
    jvmTarget = "1.8"
  }
}

tasks.withType(JavaCompile::class) {
  sourceCompatibility = JavaVersion.VERSION_1_8.toString()
  targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

gradlePlugin {
  plugins {

    create("propertiesPlugin") {
      id = "org.danbrough.kotlinxtras.properties"
      implementationClass = "org.danbrough.kotlinxtras.PropertiesPlugin"
      displayName = "KotlinXtras properties plugin"
      description = "Provides a gradle properties abstraction. (For internal use)"
    }


    create("binariesConsumer") {
      id = "org.danbrough.kotlinxtras.consumer"
      implementationClass = "org.danbrough.kotlinxtras.binaries.BinariesConsumerPlugin"
      displayName = "KotlinXtras binaries consumer plugin"
      description = "Precompiled binaries for openssl,curl for compiling against."
    }

    create("binariesProvider") {
      id = "org.danbrough.kotlinxtras.provider"
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

