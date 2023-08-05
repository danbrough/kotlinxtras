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
  deferToPrebuiltPackages = true
}

/*enableOpenssl {

}*/

val openSSL = enableOpenssl3 {
  deferToPrebuiltPackages = true
}

enableCurl(openSSL) {
  deferToPrebuiltPackages = false
}

enableSqlite {
  deferToPrebuiltPackages = false
}

enableLibSSH2(openSSL) {

}

/*
gradlePlugin {
  isAutomatedPublishing = false
}*/


