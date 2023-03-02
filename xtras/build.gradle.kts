import org.danbrough.kotlinxtras.core.enableCurl
import org.danbrough.kotlinxtras.core.enableIconv
import org.danbrough.kotlinxtras.core.enableOpenssl
import org.danbrough.kotlinxtras.core.enableSqlite

plugins {
  // `kotlin-dsl`
  //kotlin("multiplatform")
  xtras("sonatype", Xtras.version)
  xtras("core", Xtras.version)
}


enableIconv {
}

enableOpenssl {
}

enableCurl {
}

enableSqlite {
}
/*
gradlePlugin {
  isAutomatedPublishing = false
}*/


