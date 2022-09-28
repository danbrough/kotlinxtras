package demo1

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import libsqlite.*

val log = klog.klog("demo1") {
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
  level = Level.TRACE
}


fun main(args: Array<String>) {
  log.info("running main ..")


  memScoped {
    log.debug("sqlite version: ${libsqlite.sqlite3_version.toKString()}")


  }

}