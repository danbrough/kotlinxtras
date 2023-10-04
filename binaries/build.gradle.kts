import org.danbrough.xtras.curl.xtrasCurl
import org.danbrough.xtras.env.xtrasBuildEnvironment
import org.danbrough.xtras.log
import org.danbrough.xtras.mqtt.xtrasMQTT
import org.danbrough.xtras.openssl.xtrasOpenSSL
import org.jetbrains.kotlin.konan.target.Family


plugins {
  `kotlin-dsl`
  alias(libs.plugins.xtras.openssl)
  alias(libs.plugins.xtras.curl)
  alias(libs.plugins.xtras.mqtt)
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

xtrasMQTT(ssl) {
//  supportedTargets =
//    supportedTargets.filter { it.family.isAppleFamily || it.family == Family.LINUX }
}




xtrasCurl(ssl) {
}

