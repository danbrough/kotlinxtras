import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.danbrough
import org.danbrough.xtras.sonatype.sonatypePublishing
import org.danbrough.xtras.xtrasPublishing

plugins {
  `kotlin-dsl`
}

group = "$XTRAS_PACKAGE.openssl"
version = xtras.versions.xtras.openssl.get()

buildscript {
  dependencies {
    classpath(xtras.xtras.plugin)
  }
}


dependencies {
  implementation(xtras.xtras.plugin)
  compileOnly(xtras.kotlin.gradle.plugin)
}

repositories {
  danbrough()
  mavenCentral()
}



xtrasPublishing()
sonatypePublishing {

}