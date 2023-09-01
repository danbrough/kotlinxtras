package demo

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
   fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory:DriverFactory):  Database {
  val driver = driverFactory.createDriver()
  return  Database(driver)
}
