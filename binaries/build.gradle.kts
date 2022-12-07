plugins {
  //`maven-publish`
/*
  xtras("iconv", Xtras.version)
  xtras("sqlite", Xtras.version)*/

  xtras("openssl", Xtras.version)
  //xtras("curl", Xtras.version)


}

xtrasBinaries {

}

/*
xtrasIconv {
  buildEnabled = true
}



xtrasSqlite {
  buildEnabled = true
}
*/


/*
xtrasCurl {
  buildEnabled = true
}
*/

xtrasOpenssl {
  buildEnabled = true
}
