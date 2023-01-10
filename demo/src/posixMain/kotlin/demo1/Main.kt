package demo1

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import libcurl.*

val log = klog.klog("demo1") {
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
  level = Level.TRACE
}


fun main(args: Array<String>) {
  log.info("running main ..")

//  val url = if (args.isEmpty()) "https://example.com" else args[0]


  //log.debug("connecting to $url ..")

  memScoped {

    log.trace("doing stuff")
  }

}