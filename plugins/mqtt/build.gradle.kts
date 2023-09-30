import org.danbrough.xtras.env.xtrasBuildEnvironment
import org.danbrough.xtras.xtrasBuildDir

plugins {
  alias(libs.plugins.xtras)
  `kotlin-dsl`
  `maven-publish`
}


xtrasBuildEnvironment {
  javaLanguageVersion = 8
}

group = libs.versions.xtrasPackage.get()
version = libs.versions.mqtt.get()

dependencies {
  implementation(libs.xtras.plugin)
  implementation(libs.kotlin.gradle.plugin)
}


gradlePlugin {
  plugins {
    create("mqtt") {
      id = "$group.mqtt"
      implementationClass = "$group.mqtt.MQTTPlugin"
    }
  }
}

