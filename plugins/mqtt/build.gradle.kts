import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.danbrough
import org.danbrough.xtras.sonatype.sonatypePublishing
import org.danbrough.xtras.xtrasPublishing

plugins {
  `kotlin-dsl`
}

buildscript {
  dependencies {
    classpath(xtras.xtras.plugin)
  }
}

group = "$XTRAS_PACKAGE.mqtt"
version = xtras.versions.xtras.mqtt.get()

dependencies {
  api(xtras.xtras.plugin)
  implementation(xtras.kotlin.gradle.plugin)
}

repositories {
  danbrough()
  mavenCentral()
}


xtrasPublishing()
sonatypePublishing()