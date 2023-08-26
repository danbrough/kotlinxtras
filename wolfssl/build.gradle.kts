import org.danbrough.kotlinxtras.XtrasDSLMarker
import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.danbrough.kotlinxtras.source.gitSource
import org.danbrough.kotlinxtras.library.xtrasCreateLibrary

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras)
}

repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
  google()
}


kotlin {
  linuxX64()
}

object WolfSSL {
  const val extensionName = "wolfSSL"
  const val sourceURL = "https://github.com/wolfSSL/wolfssl.git"
  const val version = "5.6.3"
  const val tag = "v5.6.3-stable"
}

@XtrasDSLMarker
fun Project.xtrasWolfSSL(
  name: String = WolfSSL.extensionName,
  configure: XtrasLibrary.() -> Unit = {}
) =
  xtrasCreateLibrary(name, WolfSSL.version) {
    gitSource(WolfSSL.sourceURL, tag = WolfSSL.tag)
    configure()
  }


xtrasWolfSSL {

  buildEnvironment {

  }
}


