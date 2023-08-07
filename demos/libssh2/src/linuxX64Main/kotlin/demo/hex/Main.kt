package demo.hex

import demo.log
import io.ktor.util.decodeBase64String
import io.ktor.util.encodeBase64
import io.ktor.util.hex


fun main(args: Array<String>) {
  log.info("hexDemo..")
  hex(byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 255.toByte())).also { hexString ->
    log.debug("HEX: $hexString")
    hex(hexString).also { bytes ->
      log.debug("bytes: ${bytes.joinToString { it.toUByte().toString() }}")
    }
  }


 val msg = "cabbaqes and kings!"

  msg.encodeBase64().also {b64->
    log.debug("$msg = $b64")
    b64.decodeBase64String().also {
      log.debug("decoded: $it")
    }

  }

}