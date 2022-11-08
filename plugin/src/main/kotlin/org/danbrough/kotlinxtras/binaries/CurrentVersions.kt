package org.danbrough.kotlinxtras.binaries

object CurrentVersions {
  const val curl = "7_86_0a"
  const val openssl = "1_1_1s"
  const val sqlite = "3.39.4a"

  object iconv{
    const val version = "1.17a"
    const val src = "https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz"
  }


  private val XTRAS_GROUP = "org.danbrough.kotlinxtras"
  fun BinariesConsumerExtension.enableCurl(group:String = XTRAS_GROUP,version:String = curl){
    dependency(group,"curl",version)
  }


  fun BinariesConsumerExtension.enableOpenssl(group:String = XTRAS_GROUP,version:String = openssl){
    dependency(group,"openssl",version)
  }

  fun BinariesConsumerExtension.enableSqlite(group:String = XTRAS_GROUP,version:String = sqlite){
    dependency(group,"sqlite",version)
  }

  fun BinariesConsumerExtension.enableIconv(group:String = XTRAS_GROUP,version:String = iconv.version){
    dependency(group,"iconv",version)
  }
}


