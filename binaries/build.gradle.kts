import org.danbrough.kotlinxtras.curl.xtrasCurl
import org.danbrough.kotlinxtras.wolfssl.xtrasWolfSSL
import org.danbrough.kotlinxtras.wolfssh.xtrasWolfSSH

plugins {
  //alias(libs.plugins.kotlinXtras)
  //`java-gradle-plugin`
  `kotlin-dsl`
  alias(libs.plugins.kotlinXtras.wolfssl)
  alias(libs.plugins.kotlinXtras.wolfssh)
  alias(libs.plugins.kotlinXtras.curl)
}

val deferToMaven = false

val ssl = xtrasWolfSSL{
  resolveBinariesFromMaven = deferToMaven
}


xtrasCurl(ssl) {
  resolveBinariesFromMaven = deferToMaven
}

xtrasWolfSSH(ssl) {
  resolveBinariesFromMaven = deferToMaven
}
