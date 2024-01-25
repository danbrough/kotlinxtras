import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.mqtt.mqtt
import org.danbrough.xtras.openssl.openssl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(xtras.plugins.kotlin.multiplatform)
  `maven-publish`
}

buildscript {
  dependencies {
    classpath(xtras.xtras.openssl.plugin)
    classpath(xtras.xtras.mqtt.plugin)
  }
}

group = "$XTRAS_PACKAGE.mqtt"
version = xtras.versions.xtras.mqtt.get()

mqtt(ssl = openssl()) {
  buildEnabled = true
}



kotlin {
  declareSupportedTargets()
  jvm()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(project(":openssl"))
      implementation(xtras.org.danbrough.klog)
    }
  }

  val commonTest by sourceSets.getting {
    dependencies {
      implementation(xtras.kotlin.test)
    }
  }

  val nativeMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  val nativeTest by sourceSets.creating {
    dependsOn(commonTest)
  }

  targets.withType<KotlinNativeTarget> {
    compilations["main"].apply {
      defaultSourceSet.dependsOn(nativeMain)
    }
    compilations["test"].apply {
      defaultSourceSet.dependsOn(nativeTest)
    }
  }
}

