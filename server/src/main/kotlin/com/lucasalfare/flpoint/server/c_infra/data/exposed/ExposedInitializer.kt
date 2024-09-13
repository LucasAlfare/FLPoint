package com.lucasalfare.flpoint.server.c_infra.data.exposed

import com.lucasalfare.flpoint.server.a_domain.Constants
import org.jetbrains.exposed.sql.SchemaUtils

object ExposedInitializer {

  fun initialize() {
    AppDB.initialize(
      jdbcUrl = Constants.SQLITE_URL,
      jdbcDriverClassName = Constants.SQLITE_DRIVER,
      username = "",
      password = "",
      maximumPoolSize = Constants.DEFAULT_MAXIMUM_POOL_SIZE,
    ) {
      SchemaUtils.createMissingTablesAndColumns(Users, Points)
    }
  }
}