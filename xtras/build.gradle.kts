import org.danbrough.kotlinxtras.core.enableCurl
import org.danbrough.kotlinxtras.core.enableIconv
import org.danbrough.kotlinxtras.core.enableOpenssl3
import org.danbrough.kotlinxtras.core.enableSqlite

plugins {
  // `kotlin-dsl`
  //kotlin("multiplatform")
  xtras("sonatype", Xtras.version)
  xtras("core", Xtras.version)
}


enableIconv {

}

/*enableOpenssl {

}*/

enableOpenssl3 {

}

enableCurl {
  deferToPrebuiltPackages = false
}

enableSqlite {
  deferToPrebuiltPackages = false

}
/*
gradlePlugin {
  isAutomatedPublishing = false
}*/


