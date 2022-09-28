package demo1

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import kotlinx.cinterop.*
import libsqlite.*
import cnames.structs.sqlite3

val log = klog.klog("demo1") {
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
  level = Level.TRACE
}


fun main(args: Array<String>) {
  log.info("running main ..")

  val db = memScoped {
    val dbPtr = alloc<CPointerVar<sqlite3>>()
    val sqlFlags = SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE
    val openResult = sqlite3_open_v2(":memory:", dbPtr.ptr, sqlFlags, null)
    if (openResult != SQLITE_OK) {
      val msg = sqlite3_errmsg(dbPtr.value)?.toKString() ?: "error in open"
      log.error("open failed. error:$msg")
      throw Error(msg)
    }
    dbPtr.value!!
  }

  log.info("done .. closing db.")
  sqlite3_close(db)
}