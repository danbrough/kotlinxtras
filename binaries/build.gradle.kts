
import org.danbrough.xtras.env.xtrasBuildEnvironment
import org.danbrough.xtras.wolfssl.xtrasWolfSSL
import org.jetbrains.kotlin.konan.target.KonanTarget


plugins {
  `kotlin-dsl`
  alias(libs.plugins.xtras.wolfssl)
}

val deferToMaven = false


xtrasBuildEnvironment {
  javaLanguageVersion = 8
}


val ssl = xtrasWolfSSL {
  supportedTargets = listOf(
    KonanTarget.LINUX_X64,

  )
  resolveBinariesFromMaven = deferToMaven
}

tasks.create("thang"){
  doFirst {
    println("Thang!")
  }
}