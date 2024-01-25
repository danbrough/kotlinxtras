import org.danbrough.xtras.danbrough
import org.danbrough.xtras.sonatype.sonaRepoID
import org.danbrough.xtras.sonatype.sonatypePublishing
import org.danbrough.xtras.xtrasPublishing


plugins {
  `kotlin-dsl`
}

buildscript {
  dependencies {
    classpath(xtras.xtras.support)
  }
}

group = xtras.versions.xtrasPackage.get()
version = xtras.versions.xtras.version.get()

repositories {
  danbrough()
  mavenCentral()
}

dependencies {
  implementation(xtras.kotlin.gradle.plugin)
  api(xtras.xtras.support)
  implementation(xtras.org.danbrough.klog)
  implementation(xtras.dokka.gradle.plugin)
}

gradlePlugin {
  plugins {
    create("xtras") {
      id = group.toString()
      implementationClass = "$group.XtrasPlugin"
      displayName = "Xtras Plugin"
      description = "Kotlin multiplatform support plugin"
    }
  }
}


xtrasPublishing()
sonatypePublishing {

}


tasks.create("supportRepoID") {
  doFirst {
    sonaRepoID()
  }
}