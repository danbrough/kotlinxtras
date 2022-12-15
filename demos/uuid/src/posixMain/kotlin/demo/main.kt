package demo

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import klog.klog
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.UByteVarOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap.free
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import org.danbrough.kotlinxtras.xtras.uuid.*


val log = klog("DEMO") {
  level = Level.TRACE
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
}


fun main() {
  log.info("running main ...")



  memScoped {

    val uuid:uuid_t = allocArray<UByteVar>(16)

    uuid_generate_random(uuid)

    /*
        char uuid_str[37];      // ex. "1b4e28ba-2fa1-11d2-883f-0016d3cca427" + "\0"
        uuid_unparse_lower(uuid, uuid_str);
        printf("generate uuid=%s\n", uuid_str);
     */

    val uuidStr = allocArray<ByteVar>(37)
    uuid_unparse_lower(uuid,uuidStr)
    log.debug("UUID: ${uuidStr.toKString()}")
  }
}