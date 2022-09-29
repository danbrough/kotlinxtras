package demo1

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import kotlinx.cinterop.*
import libsqlite.*
import cnames.structs.sqlite3
import cnames.structs.sqlite3_stmt

val log = klog.klog("demo1") {
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
  level = Level.TRACE
}

private const val SQL_CREATE =
  """
    CREATE TABLE IF NOT EXISTS log(
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      `time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      message VARCHAR
    )
  """

private fun checkReturnValue(
  db: CPointerVar<sqlite3>,
  rc: Int,
  defaultMessage: String = "Unknown error"
): String? = if (rc != SQLITE_OK) {
  sqlite3_errmsg(db.value)?.toKString() ?: defaultMessage
}
else
  null


fun MemScope.processStatement(db: CPointerVar<sqlite3>, sql: String, processStmt:(CPointerVar<sqlite3_stmt>)->Unit = {}) {
  val stmtPtr = alloc<CPointerVar<sqlite3_stmt>>()
  log.debug("processStatement(): $sql")
  runCatching {
    checkReturnValue(
      db,
      sqlite3_prepare_v2(db.value, sql, -1, stmtPtr.ptr, null),
      "Prepare failed."
    )?.also {
      log.error("prepare failed: $it")
      return@runCatching
    }
    log.trace("statement prepared")


    while (sqlite3_step(stmtPtr.value) != SQLITE_DONE){
      processStmt(stmtPtr)
    }

  }.exceptionOrNull().also {
    checkReturnValue(db, sqlite3_finalize(stmtPtr.value), "Finalize failed")
    if (it != null) throw it
  }
}

fun main(args: Array<String>) {
  log.info("running main ..")

  val logMessage = if (args.isNotEmpty()) "\"${args[0]}\"" else "'log message at ' || CURRENT_TIMESTAMP"


  memScoped {

    val dbPtr = alloc<CPointerVar<sqlite3>>()
    runCatching {
      val sqlFlags = SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE
      checkReturnValue(
        dbPtr,
        sqlite3_open_v2("test.db", dbPtr.ptr, sqlFlags, null),
        "Error in open"
      )?.also {
        log.error("open failed. error:$it")
        sqlite3_close(dbPtr.value)
        throw Error(it)
      }

      processStatement(dbPtr, SQL_CREATE)

      processStatement(dbPtr,"INSERT INTO log(message) values ($logMessage)")

      processStatement(dbPtr, "SELECT * FROM log"){
        val id = sqlite3_column_int64(it.value,0)
        val date = sqlite3_column_text(it.value,1)?.reinterpret<ByteVar>()?.toKString()
        val msg = sqlite3_column_text(it.value,2)?.reinterpret<ByteVar>()?.toKString()
        log.info("$id:$date:\t$msg")
      }



/*    val stmt = memScoped {
      val stmtPtr = alloc<CPointerVar<sqlite3_stmt>>()
      val rc = sqlite3_prepare_v2(dbPtr.value, SQL_CREATE, -1, stmtPtr.ptr, null)
      if (rc != SQLITE_OK) {
        val msg = sqlite3_errmsg(dbPtr.value)?.toKString() ?: "error in prepare "
        log.error("prepare failed. error:$msg")
        sqlite3_close(dbPtr.value)
        throw Error(msg)
      }
      stmtPtr.value!!
    }

    log.debug("prepared statement")
    when (val rc = sqlite3_step(stmt)) {
      SQLITE_ROW -> log.trace("SQLITE_ROW")
      SQLITE_DONE -> log.trace("SQLITE_DONE")
      else -> log.trace("got rc: $rc")
    }

    if (sqlite3_finalize(stmt) != SQLITE_OK) {
      val msg = sqlite3_errmsg(dbPtr.value)?.toKString() ?: "error in finalize"
      log.error("finalize failed. error:$msg")
    }*/

    }.exceptionOrNull().also {

      log.info("done .. closing db.")
      checkReturnValue(dbPtr, sqlite3_close(dbPtr.value))?.also {
        log.warn("failed close db: $it")
      }
      if (it != null) log.error(it.message, it)
    }
  }
}