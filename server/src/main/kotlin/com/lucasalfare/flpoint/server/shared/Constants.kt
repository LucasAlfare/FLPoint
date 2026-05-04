package com.lucasalfare.flpoint.server.shared

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Constants {
  companion object {
    val logger: Logger = LoggerFactory.getLogger("App")!!

    const val DEFAULT_MIN_PASSWORD_LENGTH = 4

    const val DEFAULT_JWT_EXPIRATION_TIME = 10 // minutes

    const val DATABASE_SQLITE_URL = "jdbc:sqlite:./data.db"
    const val DATABASE_SQLITE_DRIVER = "org.sqlite.JDBC"

    // in memory H2, for testing
//    const val DATABASE_H2_URL = "jdbc:h2:mem:regular"
//    const val DATABASE_H2_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;"
    const val DATABASE_H2_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;"
    const val DATABASE_H2_DRIVER = "org.h2.Driver"
  }
}
