import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.danbrough
import org.danbrough.xtras.enableVerboseTesting
import org.danbrough.xtras.sonatype.sonaRepoID
import org.danbrough.xtras.sonatype.sonatypePublishing
import org.danbrough.xtras.xtrasPublishing

plugins {
  alias(xtras.plugins.kotlin.multiplatform) apply false
  `maven-publish`
}

buildscript {
  dependencies {
    classpath(xtras.xtras.plugin)
  }
}

val xtrasProjectGroup = XTRAS_PACKAGE
val xtrasProjectVersion = xtras.versions.xtras.version.get()


allprojects {
  group = xtrasProjectGroup
  version = xtrasProjectVersion

  repositories {
    danbrough()
    mavenCentral()
  }


  xtrasPublishing()
  sonatypePublishing {

  }
  enableVerboseTesting()
}


tasks.create("rootRepoID") {
  doFirst {
    sonaRepoID()
  }
}