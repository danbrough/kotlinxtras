plugins {
  xtras("sonatype")

  xtras("iconv", Xtras.version)
  xtras("openssl", Xtras.version)
  xtras("sqlite", Xtras.version)
 // xtras("curl", Xtras.version)


}

xtrasBinaries {

}

xtrasIconv {
  buildEnabled = true
}

xtrasOpenssl {
  buildEnabled = true
}

xtrasSqlite {
  buildEnabled = true
}









