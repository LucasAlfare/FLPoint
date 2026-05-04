package com.lucasalfare.flpoint.server.config

import com.lucasalfare.flpoint.server.infrastructure.persistence.AppDB
import com.lucasalfare.flpoint.server.infrastructure.persistence.SchemaUtils
import com.lucasalfare.flpoint.server.infrastructure.persistence.Users
import com.lucasalfare.flpoint.server.infrastructure.persistence.Points
import com.lucasalfare.flpoint.server.infrastructure.persistence.JwtBlacklist
import com.lucasalfare.flpoint.server.infrastructure.persistence.ExposedDataCRUD
import com.lucasalfare.flpoint.server.domain.model.CreateUserRequestDTO
import com.lucasalfare.flpoint.server.application.usecase.AppUsecases
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

fun initializeDatabase(
  jdbcUrl: String,
  jdbcDriverClassName: String,
  username: String,
  password: String,
  maximumPoolSize: Int,
  isDev: Boolean
) {
  AppDB.initialize(
    jdbcUrl = jdbcUrl,
    jdbcDriverClassName = jdbcDriverClassName,
    username = username,
    password = password,
    maximumPoolSize = maximumPoolSize
  ) {
    SchemaUtils.createMissingTablesAndColumns(SchemaUtils.Users, SchemaUtils.Points, SchemaUtils.JwtBlacklist)

    runCatching {
      runBlocking {
        if (isDev) {
          Constants.logger.debug("Creating default DEV users...")

          AppUsecases.signupUser(
            createUserRequestDTO = CreateUserRequestDTO(
              name = "Dev Admin",
              email = "admin@dev.com",
              plainPassword = "123456",
              timeZone = TimeZone.of("America/Sao_Paulo")
            ),
            isAdmin = true
          )

          AppUsecases.signupUser(
            createUserRequestDTO = CreateUserRequestDTO(
              name = "Dev User",
              email = "user@dev.com",
              plainPassword = "123456",
              timeZone = TimeZone.of("America/Sao_Paulo")
            ),
            isAdmin = false
          )
        }

        ExposedDataCRUD.deleteExpiredBlacklistedTokens()
      }
    }.onSuccess {
      Constants.logger.debug("Initial users created (or already existed)")
    }.onFailure {
      Constants.logger.debug("User initialization skipped: ${it.message}")
    }
  }
}
