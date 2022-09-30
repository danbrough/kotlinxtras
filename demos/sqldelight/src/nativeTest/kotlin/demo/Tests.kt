package demo

import klog.*
import kotlin.test.Test

val log by lazy {
  klog("demo"){
    level = Level.TRACE
    messageFormatter = KMessageFormatters.verbose.colored
    writer = KLogWriters.stdOut
  }
}

class Tests {

  @Test
  fun test1(){
    log.info("test1()")
    val db = createDatabase(DriverFactory())
    log.debug("got db: $db")

  }
}