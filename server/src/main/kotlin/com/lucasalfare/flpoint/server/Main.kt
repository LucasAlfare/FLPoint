package com.lucasalfare.flpoint.server

import com.lucasalfare.flpoint.server.a_domain.PasswordHasher
import com.lucasalfare.flpoint.server.a_domain.PointsHandler
import com.lucasalfare.flpoint.server.a_domain.UsersHandler
import com.lucasalfare.flpoint.server.b_usecase.PointUsecases
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import com.lucasalfare.flpoint.server.c_infra.data.exposed.ExposedInitializer
import com.lucasalfare.flpoint.server.c_infra.data.exposed.ExposedPointsHandler
import com.lucasalfare.flpoint.server.c_infra.data.exposed.ExposedUsersHandler
import com.lucasalfare.flpoint.server.c_infra.security.hashing.jbcrypt.JBCryptPasswordHasher
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.KtorLauncher

/**
 * Entry point for the application.
 *
 * This function sets up and starts the application by initializing necessary components and configurations.
 * It performs the following tasks:
 * - Creates instances of `UsersHandler` and `PointsHandler` for handling user and point operations.
 * - Creates an instance of `PasswordHasher` for hashing user passwords.
 * - Initializes `PointUsecases` and `UserUsecases` with the respective handlers and password hasher.
 * - Initializes the database using `ExposedInitializer`.
 * - Launches the Ktor server with the configured use cases.
 */
fun main() {
  val usersHandler: UsersHandler = ExposedUsersHandler
  val pointsHandler: PointsHandler = ExposedPointsHandler
  val passwordHasher: PasswordHasher = JBCryptPasswordHasher
  val pointUsecases = PointUsecases(pointsHandler)
  val userUsecases = UserUsecases(
    usersHandler,
    passwordHasher
  )

  ExposedInitializer.initialize()
  KtorLauncher(userUsecases, pointUsecases).launch()
}