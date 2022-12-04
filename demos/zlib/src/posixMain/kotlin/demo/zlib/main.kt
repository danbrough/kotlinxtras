package demo.zlib

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import klog.klog


val log = klog("DEMO"){
  level = Level.TRACE
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
}


fun main(){
  log.info("running main ...")
}