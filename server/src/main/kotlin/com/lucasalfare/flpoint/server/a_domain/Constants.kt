package com.lucasalfare.flpoint.server.a_domain

/**
 * A utility class that holds constant values used throughout the application.
 *
 * This class contains constants related to SQLite database configuration, such as the database URL and driver.
 */
class Constants {
  companion object {
    /**
     * The URL for the SQLite database.
     *
     * This URL is used to connect to the SQLite database located at `./data.db`.
     */
    const val SQLITE_URL = "jdbc:sqlite:./data.db"

    /**
     * The driver class name for SQLite.
     *
     * This is the class name used by the JDBC driver to connect to the SQLite database.
     */
    const val SQLITE_DRIVER = "org.sqlite.JDBC"

    /**
     * The default maximum pool size for database connections.
     *
     * This is used if none is manually defined.
     */
    const val DEFAULT_MAXIMUM_POOL_SIZE = 6

    /**
     * The default port number for the web server.
     *
     * This port is used if none is manually defined.
     */
    const val DEFAULT_WEB_SERVER_PORT = 80

    const val EMAIL_ADDRESS_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
  }
}