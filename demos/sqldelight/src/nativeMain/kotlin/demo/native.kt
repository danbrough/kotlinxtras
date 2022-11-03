package demo

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration


actual class DriverFactory {
  actual fun createDriver(): SqlDriver {
    val schema = Database.Schema

    return NativeSqliteDriver(
      DatabaseConfiguration(
        name = "hockey.db",
        version = schema.version,
        extendedConfig = DatabaseConfiguration.Extended(basePath = "./"),
        create = { connection ->
          wrapConnection(connection) { schema.create(it) }
        },
        upgrade = { connection, oldVersion, newVersion ->
          wrapConnection(connection) { schema.migrate(it, oldVersion, newVersion) }
        },
      )
    )
  }
}