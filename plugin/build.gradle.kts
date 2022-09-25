
plugins {
  kotlin("jvm")
  `java-gradle-plugin`
  `maven-publish`
  id("org.jetbrains.dokka")
}


kotlin {
  dependencies {
    //implementation(gradleApi())
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

    create("binaries") {
      id = "org.danbrough.kotlinxtras.binaries"
      implementationClass = "org.danbrough.kotlinxtras.binaries.BinariesPlugin"
      displayName = "KotlinXtras binaries plugin"
      description = "Precompiled binaries for openssl,curl for compiling against."
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
  repositories {
    maven("../build/m2"){
      name = "m2"
    }
  }

  publications.all {
    if (this !is MavenPublication) return@all
    if (project.hasProperty("publishDocs"))
      artifact(javadocJar)
    artifact(sourcesJar)
  }
}

