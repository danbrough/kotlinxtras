import org.danbrough.xtras.curl.xtrasCurl
import org.danbrough.xtras.env.xtrasBuildEnvironment
import org.danbrough.xtras.log
import org.danbrough.xtras.openssl.xtrasOpenSSL


plugins {
  `kotlin-dsl`
  alias(libs.plugins.xtras.openssl)
  alias(libs.plugins.xtras.curl)
  `maven-publish`
}


val deferToMaven = false


xtrasBuildEnvironment {
  javaLanguageVersion = 8
}

val ssl = xtrasOpenSSL {
  resolveBinariesFromMaven = deferToMaven
  project.log("xtrasOpenSSL configured")
}

xtrasCurl(ssl) {
}

