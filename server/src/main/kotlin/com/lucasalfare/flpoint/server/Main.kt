package com.lucasalfare.flpoint.server

import com.lucasalfare.flpoint.server.a_domain.PasswordHasher
import com.lucasalfare.flpoint.server.a_domain.TimeRegistrationsHandler
import com.lucasalfare.flpoint.server.a_domain.UsersHandler
import com.lucasalfare.flpoint.server.b_usecase.TimeRegistrationsUseCases
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import com.lucasalfare.flpoint.server.c_infra.data.exposed.ExposedInitializer
import com.lucasalfare.flpoint.server.c_infra.data.exposed.ExposedTimeRegistrationsHandler
import com.lucasalfare.flpoint.server.c_infra.data.exposed.ExposedUsersHandler
import com.lucasalfare.flpoint.server.c_infra.security.hashing.jbcrypt.JBCryptPasswordHasher
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.KtorLauncher

fun main() {
  val usersHandler: UsersHandler = ExposedUsersHandler
  val timeRegistrationsHandler: TimeRegistrationsHandler = ExposedTimeRegistrationsHandler
  val passwordHasher: PasswordHasher = JBCryptPasswordHasher

  val userUsecases = UserUsecases(
    usersHandler,
    passwordHasher
  )

  val timeRegistrationsUseCases = TimeRegistrationsUseCases(
    timeRegistrationsHandler
  )

  ExposedInitializer.initialize()
  KtorLauncher(
    userUsecases,
    timeRegistrationsUseCases
  ).launch()
}