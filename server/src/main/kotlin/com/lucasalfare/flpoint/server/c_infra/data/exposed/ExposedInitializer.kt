package com.lucasalfare.flpoint.server.c_infra.data.exposed

import com.lucasalfare.flpoint.server.a_domain.Constants
import com.lucasalfare.flpoint.server.a_domain.EnvsLoader.loadEnv
import org.jetbrains.exposed.sql.SchemaUtils

/**
 * Object responsible for initializing the database connection and schema.
 *
 * The `ExposedInitializer` provides a method to set up the database connection using specific configuration parameters,
 * and to ensure that the necessary database tables and columns are created.
 */
object ExposedInitializer {

  /**
   * Initializes the database connection and creates the necessary schema.
   *
   * This method configures the database connection using the provided JDBC URL, JDBC driver class name,
   * and other parameters. It then creates any missing tables and columns defined in the `Users` and `Points` schemas.
   */
  fun initialize() {
    AppDB.initialize(
      jdbcUrl = loadEnv("DATABASE_JDBC_URL"),
      jdbcDriverClassName = loadEnv("DATABASE_JDBC_CLASS_NAME"),
      username = loadEnv("DATABASE_USERNAME"),
      password = loadEnv("DATABASE_PASSWORD"),
      maximumPoolSize = Constants.DEFAULT_MAXIMUM_POOL_SIZE,
    ) {
      SchemaUtils.createMissingTablesAndColumns(Users, Points)
    }
  }
}