
import org.danbrough.xtras.env.xtrasBuildEnvironment
import org.danbrough.xtras.log
import org.danbrough.xtras.wolfssl.xtrasWolfSSL
import org.jetbrains.kotlin.konan.target.KonanTarget


plugins {
  `kotlin-dsl`
  alias(libs.plugins.xtras.wolfssl)
}

val deferToMaven = false


xtrasBuildEnvironment {
  javaLanguageVersion = 8
  project.log("xtrasBuildEnvironment configured")
}


val ssl = xtrasWolfSSL {
  resolveBinariesFromMaven = deferToMaven
  project.log("xtrasWolfSSL configured")

}

tasks.create("thang"){
  doFirst {
    println("Thang!")
  }
}