
import org.danbrough.xtras.wolfssl.xtrasWolfSSL


plugins {
  `kotlin-dsl`
  alias(libs.plugins.xtras.wolfssl)
}

val deferToMaven = false


val ssl = xtrasWolfSSL {
  resolveBinariesFromMaven = deferToMaven
}

tasks.create("thang"){
  doFirst {
    println("Thang!")
  }
}