pluginManagement {
  repositories {
    maven("/usr/local/kotlinxtras/build/xtras/maven")
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}


val include: String? by settings




/*
if (include == null || include == "plugins") {
  listOf(
    "curl",
   "wolfssl",
    "wolfssh",
    "openssl",
  ).forEach {
    include(":plugins:$it")
    project(":plugins:$it").projectDir = rootDir.resolve("plugins/$it")
  }
}

if (include == null || include == "binaries")
  include(":binaries")

if (include == null || include == "demos"){
  listOf("curl").forEach {
    include(":demos:${it}_demo")
    project(":demos:${it}_demo").projectDir = rootDir.resolve("demos/$it")
  }
}*/
dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}
