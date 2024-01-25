package tests

import klog.KLog
import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import klog.klog
import kotlin.test.Test

val log = klog("TESTS") {
  level = Level.TRACE
  messageFormatter = KMessageFormatters.verbose.colored
  writer = KLogWriters.stdOut
}


class Tests {
  @Test
  fun test1() {
    log.info("test1()")
  }
}