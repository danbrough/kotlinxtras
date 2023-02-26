import org.danbrough.kotlinxtras.enableCurl
import org.danbrough.kotlinxtras.enableIconv
import org.danbrough.kotlinxtras.enableOpenssl
import org.danbrough.kotlinxtras.enableSqlite

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



