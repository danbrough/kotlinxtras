
import org.danbrough.kotlinxtras.wolfssl.xtrasWolfSSL


plugins {
  `kotlin-dsl`
  alias(libs.plugins.kotlinXtras.wolfssl) apply false
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