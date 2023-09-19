package demo

import klog.*


val log = klog("DEMO") {
  messageFormatter = KMessageFormatters.verbose.colored
  writer = KLogWriters.stdOut
  level = Level.TRACE
}