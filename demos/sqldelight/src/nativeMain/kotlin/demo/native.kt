package demo

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration


actual class DriverFactory {
  actual fun createDriver(): SqlDriver {
    NativeSqliteDriver(DatabaseConfiguration())
    return NativeSqliteDriver(Database.Schema, "hockey.db")
  }
}