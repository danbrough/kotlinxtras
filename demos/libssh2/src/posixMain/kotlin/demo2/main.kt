package demo2

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored

private val log = klog.klog("demo2") {
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
  level = Level.TRACE
}

fun main(args: Array<String>) {
  log.info("demo2()")
  
}