plugins {
  xtras("sonatype")

  xtras("iconv", Xtras.version)
  xtras("openssl", Xtras.version)
  xtras("sqlite", Xtras.version)

}

xtrasBinaries {

}

xtrasIconv {
  buildEnabled = true
}

xtrasOpenssl {
  buildEnabled = true
}
/*

xtrasCurl {
  buildEnabled = true
}
*/

xtrasSqlite {
  buildEnabled = true
}









