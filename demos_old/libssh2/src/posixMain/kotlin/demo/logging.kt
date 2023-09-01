package demo

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored

val log = klog.klog("demo") {
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
  level = Level.TRACE
}
