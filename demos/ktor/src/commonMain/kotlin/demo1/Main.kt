package demo1

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored


private val log = klog.klog("demo1"){
  level = Level.TRACE
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
}


fun main(args:Array<String>){
  log.debug("running demo1")
}