package demo

import com.squareup.sqldelight.db.SqlDriver

expect class DriverFactory {
   fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory:DriverFactory):  Database {
  val driver = driverFactory.createDriver()
  return  Database(driver)
}
