import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.openssl.openssl

plugins {
  alias(xtras.plugins.kotlin.multiplatform)
  `maven-publish`
}

buildscript {
  dependencies {
    classpath(xtras.xtras.openssl.plugin)
  }
}

group = "$XTRAS_PACKAGE.openssl"
version = xtras.versions.xtras.openssl.get()

openssl {
  buildEnabled = true
}


kotlin {
  declareSupportedTargets()
  jvm()
}

