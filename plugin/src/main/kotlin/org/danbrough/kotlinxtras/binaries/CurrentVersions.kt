package org.danbrough.kotlinxtras.binaries

object CurrentVersions {
  const val curl = "7_86_0a"
  const val openssl = "1_1_1s"

  object sqlite {
    const val version = "3.39.4a"
    const val url = "https://www.sqlite.org/2022/sqlite-autoconf-3390400.tar.gz"
  }

  object iconv {
    const val version = "1.17c"
    const val url = "https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz"
  }

  const val XTRAS_GROUP = "org.danbrough.kotlinxtras"

  fun BinariesConsumerExtension.enableCurl(version:String = curl,group:String = XTRAS_GROUP){
    dependency(group,"curl",version)
  }


  fun BinariesConsumerExtension.enableOpenssl(version:String = openssl,group:String = XTRAS_GROUP){
    dependency(group,"openssl",version)
  }

  fun BinariesConsumerExtension.enableSqlite(version:String = sqlite.version ,group:String = XTRAS_GROUP){
    dependency(group,"sqlite",version)
  }

  fun BinariesConsumerExtension.enableIconv(version:String = iconv.version,group:String = XTRAS_GROUP){
    dependency(group,"iconv",version)
  }
}


