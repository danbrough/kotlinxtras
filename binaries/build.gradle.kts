import org.danbrough.kotlinxtras.core.enableCurl
import org.danbrough.kotlinxtras.core.enableIconv
import org.danbrough.kotlinxtras.core.enableLibSSH2
import org.danbrough.kotlinxtras.core.enableOpenssl3
import org.danbrough.kotlinxtras.core.enableSqlite

plugins {
  // `kotlin-dsl`
  //kotlin("multiplatform")
  xtras("sonatype", Xtras.version)
  xtras("core", Xtras.version)
}


enableIconv {
  publishBinaries = true
}

val openSSL = enableOpenssl3 {
  publishBinaries = true
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

/*
gradlePlugin {
  isAutomatedPublishing = false
}*/


