import org.danbrough.kotlinxtras.enableCurl
import org.danbrough.kotlinxtras.enableOpenssl
import org.danbrough.kotlinxtras.enableSqlite
import org.danbrough.kotlinxtras.enableIconv

plugins {
  `kotlin-dsl`
  xtras("sonatype", Xtras.version)
  xtras("core", Xtras.version)
}

dependencies {
  implementation(project(":core"))
}

enableIconv{
}

enableOpenssl{
}

enableCurl{
  enablePrebuiltPackages = false
}

enableSqlite{
}

gradlePlugin {
  isAutomatedPublishing = false
}

