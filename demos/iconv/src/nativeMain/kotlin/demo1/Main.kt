package demo1


import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import libiconv.libiconv_open
import platform.iconv.iconv_t
import platform.posix.errno


private val log = klog.klog("demo1") {
  level = Level.TRACE
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
}

const val EUCSET = "EUC-JP"
const val OUTSET = "UTF-8"

fun initialize() {
  log.info("initialize()")

  val s: iconv_t = libiconv_open(OUTSET, EUCSET).also {
    if (it == null) {
      log.error("errno: $errno")
      return
    }
  }!!

  log.debug("opened iconv")
}

fun main(args: Array<String>) {
  val input = if (args.isEmpty()) "\\xB6\\xE2ʸ\\xC2\\xCE" else args[0]
  log.debug("running demo1: input: $input")

  initialize()
}