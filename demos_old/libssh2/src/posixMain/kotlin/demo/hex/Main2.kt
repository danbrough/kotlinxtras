package demo.hex

import demo.log
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import libssh2.ptr_test
import org.danbrough.utils.io.fromBase64
import org.danbrough.utils.io.fromHex
import org.danbrough.utils.io.toBase64
import org.danbrough.utils.io.toHex


fun base64Test(bytes: ByteArray) {
  val b64 = bytes.toBase64()
  log.debug("b64: $b64")
  val bytes2 = b64.fromBase64()
  log.debug("same: ${bytes.contentEquals(bytes2)}")
}


fun hexTest(bytes: ByteArray) {
  bytes.toHex().also { hexString ->
    log.debug("HEX: $hexString")
    hexString.fromHex().also {
      if (!it.contentEquals(bytes)) error("Content not equal")
    }
  }
}


@OptIn(ExperimentalForeignApi::class)
fun main2(args: Array<String>) {
  log.info("hexDemo..")
  /*
    hexTest(byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 255.toByte()))
    hexTest(Random.nextBytes(128))

    val msg = "Cabbaqes and Kings!"
    base64Test(msg.encodeToByteArray())
    base64Test(Random.nextBytes(1024))*/
  //CValuesRef<CPointerVar<ByteVar>>

  memScoped {
    //val p = cValue<CPointerVar<ByteVar>>()
    val p = cValue<CPointerVar<ByteVar>>()
    ptr_test(p.ptr)
    println("kotlin message:<${p.ptr[0]?.toKString()}>")
  }


}