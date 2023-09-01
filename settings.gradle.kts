pluginManagement {
  repositories {
    maven(file("build/xtras/maven"))
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}



plugins {
  id("de.fayard.refreshVersions") version "0.60.2"
  id("org.gradle.toolchains.foojay-resolver-convention") version ("0.4.0")
}
/*toolchainManagement {
  jvm {
    javaRepositories {
      repository("foojay") {
        resolverClass.set(org.gradle.toolchains.foojay.FoojayToolchainResolver::class.java)
      }
    }
  }
}*/
val bootstrap: String? by settings

rootProject.name = "kotlinxtras"

//includeBuild("plugin")

include(":plugin")
//project(":plugin").projectDir = rootDir.resolve("plugin2")
//include(":core:wolfssl")
if (bootstrap == null) {
  listOf("curl", "wolfssl").forEach {
    include(":$it")
    project(":$it").projectDir = rootDir.resolve("core/$it")
  }
  include(":binaries")
}
//include(":test")
/*
val pluginsOnly: String? by settings

//-PpluginsOnly=true or not specified
if (pluginsOnly == null || pluginsOnly.toBoolean()) {
  include(":plugin")
  include(":core")
}

//-PpluginsOnly=false or not specified
if (!pluginsOnly.toBoolean()) {
  //include(":libssh2")
  include(":utils")
  include(":common")
  include(":binaries")
}



*/

