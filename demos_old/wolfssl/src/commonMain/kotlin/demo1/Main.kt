package demo1


import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import klog.klog


private val log = klog("TESTS") {
  level = Level.TRACE
  messageFormatter = KMessageFormatters.verbose.colored
  writer = KLogWriters.stdOut
}

fun main() {
  log.info("main1()")
}