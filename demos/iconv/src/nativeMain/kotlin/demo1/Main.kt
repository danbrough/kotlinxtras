package demo1


import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.toKString
import libiconv.libiconv_close
import libiconv.libiconv_open
import libiconv.libiconv_t
import platform.iconv.iconv_t
import platform.posix.EINVAL
import platform.posix.errno
import platform.posix.strerror


private val log = klog.klog("demo1") {
  level = Level.TRACE
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
}

const val EUCSET = "EUC-JP"
const val OUTSET = "UTF-8"

fun initialize(): CPointer<out CPointed> {
  log.info("initialize()")
  return libiconv_open(OUTSET, EUCSET).also {
    if (errno == EINVAL){
      log.error("Initialization failure: ${strerror(errno)?.toKString()}")
      throw Error("Initialization failure: ${strerror(errno)?.toKString()}")
    }
    log.trace("errno: $errno  ${strerror(errno)?.toKString()}")
  }!!
}

fun main() {
  val input =  intArrayOf(0xb6,0xe2,0xca,0xb8,0xc2,0xce).map{it.toByte()}
  log.debug("running demo1: input: $input")

  val convDesc:libiconv_t = initialize()



}