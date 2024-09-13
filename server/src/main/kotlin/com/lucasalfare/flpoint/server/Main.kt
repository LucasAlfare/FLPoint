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