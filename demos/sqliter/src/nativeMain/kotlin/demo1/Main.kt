package demo1

import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.createDatabaseManager
import co.touchlab.sqliter.withConnection
import co.touchlab.sqliter.withStatement
import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import kotlinx.cinterop.cstr
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.toKString
import platform.posix.dirname
import platform.posix.posix_FD_ISSET
import platform.posix.realpath


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

fun main(args: Array<String>) {
  log.info("running main ..")



  val basePath = realpath(".",null)?.toKString() ?: throw Error("Failed to get current directory path")


  val config = DatabaseConfiguration(
    name = "sqliterDemo.db",
    version = 1,
    extendedConfig = DatabaseConfiguration.Extended(basePath = basePath),
    create = { db ->
      db.withStatement(SQL_CREATE) {
        execute()
        log.trace("SQL_CREATE executed")
      }
    },
    upgrade = { _, _, _ ->
//      updateCalled.increment()
//      println("updateCalled $updateCalled")
    }
  )

  val dbManager = createDatabaseManager(config)
  log.debug("created dbManager: $dbManager")


  dbManager.withConnection {
    //it.withStatement("SELECT * FROM log")
    if (args.isNotEmpty()) {
      it.withStatement("INSERT INTO log(message) VALUES(?)"){
        bindString(1,args[0])
        executeInsert()
      }
    } else {
      it.withStatement("INSERT INTO log(message) VALUES('Log message from ' || CURRENT_TIMESTAMP)"){
        executeInsert()
      }
    }


    it.withStatement("SELECT * FROM log") {
      val cursor = query()
      while (cursor.next()) {
        log.trace("${cursor.getLong(0)}:${cursor.getString(1)}\t${cursor.getString(2)}")
      }
    }
  }



}