
plugins {
  kotlin("jvm")
  `java-gradle-plugin`
  `maven-publish`
  id("org.jetbrains.dokka")
}

group = "org.danbrough.kotlinxtras"
version = "0.0.1-alpha17"

java {
  dependencies {
    implementation(gradleApi())
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
  }
}


/*
task sourceJar(type: Jar) {
  classifier 'sources'
  from sourceSets.main.allJava
}
*/


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
  repositories {
    maven("../build/m2"){
      name = "m2"
    }
  }

  publications.all {
    if (this !is MavenPublication) return@all
    artifact(javadocJar)
    artifact(sourcesJar)
  }
}

