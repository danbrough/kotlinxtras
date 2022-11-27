
plugins {
  id("org.danbrough.kotlinxtras.iconv")
  id("org.danbrough.kotlinxtras.sqlite")
  id("org.danbrough.kotlinxtras.openssl")
  id("org.danbrough.kotlinxtras.curl")
  id("org.danbrough.kotlinxtras.sonatype")
  `maven-publish`
}

xtrasIconv {
  buildEnabled = true
}

xtrasSqlite {
  buildEnabled = true
}

xtrasOpenssl {
  buildEnabled = true
}

xtrasCurl {
  buildEnabled = true
}