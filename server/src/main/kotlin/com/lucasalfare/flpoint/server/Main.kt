package com.lucasalfare.flpoint.server

import com.lucasalfare.flpoint.server.config.initializeDatabase
import com.lucasalfare.flpoint.server.config.initKtorConfiguration
import com.lucasalfare.flpoint.server.infrastructure.security.JwtGenerator
import com.lucasalfare.flpoint.server.shared.Constants
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
  val appEnv = System.getenv("APP_ENV")?.lowercase() ?: "dev"
  val isDev = appEnv == "dev"

  Constants.logger.debug("Starting application in environment: $appEnv")

  val jdbcUrl =
    if (isDev) Constants.DATABASE_SQLITE_URL
    else System.getenv("DATABASE_JDBC_URL")
      ?: error("DATABASE_JDBC_URL not defined")

  val jdbcDriver =
    if (isDev) Constants.DATABASE_SQLITE_DRIVER
    else System.getenv("DATABASE_JDBC_CLASS_NAME")
      ?: error("DATABASE_JDBC_CLASS_NAME not defined")

  val dbUser =
    if (isDev) ""
    else System.getenv("DATABASE_USERNAME") ?: ""

  val dbPass =
    if (isDev) ""
    else System.getenv("DATABASE_PASSWORD") ?: ""

  val serverPort =
    if (isDev) 7171
    else System.getenv("WEBSERVER_PORT")?.toIntOrNull()
      ?: error("WEBSERVER_PORT not defined or invalid")

  JwtGenerator.initialize(
    secret = if (isDev) {
      "dev-secret"
    } else {
      System.getenv("JWT_ALGORITHM_SIGN_SECRET")
        ?: error("JWT_ALGORITHM_SIGN_SECRET not defined")
    }
  )

  Constants.logger.info("Connecting using JDBC driver $jdbcDriver")

  initializeDatabase(
    jdbcUrl = jdbcUrl,
    jdbcDriverClassName = jdbcDriver,
    username = dbUser,
    password = dbPass,
    maximumPoolSize = 5,
    isDev = isDev
  )

  embeddedServer(factory = Netty, port = serverPort) {
    initKtorConfiguration()
  }.start(true)
}