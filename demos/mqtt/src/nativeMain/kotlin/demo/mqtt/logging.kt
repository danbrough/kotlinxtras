package demo.mqtt

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import klog.klog

val log = klog("DEMO"){
  level = Level.TRACE
  messageFormatter = KMessageFormatters.verbose.colored
  writer = KLogWriters.stdOut
}