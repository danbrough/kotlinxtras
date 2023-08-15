import org.danbrough.kotlinxtras.core.enableCurl
import org.danbrough.kotlinxtras.core.enableIconv
import org.danbrough.kotlinxtras.core.enableLibSSH2
import org.danbrough.kotlinxtras.core.enableOpenssl3
import org.danbrough.kotlinxtras.core.enableSqlite
import org.danbrough.kotlinxtras.core.enableWolfSSL
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  alias(libs.plugins.kotlinxtras.sonatype)
  alias(libs.plugins.kotlinxtras.core)
}


enableIconv {
  publishBinaries = true
}

val openSSL = enableOpenssl3 {
  publishBinaries = true
  println("OPENSSL BUILD TARGETS: $supportedTargets")

}

enableCurl(openSSL) {
  publishBinaries = true
}

enableSqlite {
  publishBinaries = true
}

enableLibSSH2(openSSL) {
  publishBinaries = true
}

enableWolfSSL {
  publishBinaries = true
}


/*
gradlePlugin {
  isAutomatedPublishing = false
}*/


