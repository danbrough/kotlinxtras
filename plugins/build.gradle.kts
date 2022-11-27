
plugins {
  setOf("iconv", "sqlite", "openssl", "curl").forEach {
    id("org.danbrough.kotlinxtras.$it")
  }
  id("org.danbrough.kotlinxtras.sonatype")
  `maven-publish`
}

