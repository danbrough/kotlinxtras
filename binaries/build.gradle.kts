import org.danbrough.kotlinxtras.curl.xtrasCurl
import org.danbrough.kotlinxtras.wolfssl.xtrasWolfSSL

plugins {
  //alias(libs.plugins.kotlinXtras)
  //`java-gradle-plugin`
  `kotlin-dsl`
  alias(libs.plugins.kotlinXtras.wolfssl)
  alias(libs.plugins.kotlinXtras.curl)
}


val ssl = xtrasWolfSSL{

}


xtrasCurl(ssl) {

}
