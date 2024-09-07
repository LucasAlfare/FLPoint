package com.lucasalfare.flpoint.server

import com.lucasalfare.flpoint.server.a_domain.JwtGenerator
import com.lucasalfare.flpoint.server.a_domain.PasswordHashing
import com.lucasalfare.flpoint.server.a_domain.UsersHandler
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import com.lucasalfare.flpoint.server.c_infra.data.exposed.ExposedInitializer
import com.lucasalfare.flpoint.server.c_infra.data.exposed.ExposedUsersHandler
import com.lucasalfare.flpoint.server.c_infra.security.hashing.jbcrypt.JBCryptPasswordHashing
import com.lucasalfare.flpoint.server.c_infra.security.jwt.ktor.KtorJwtGenerator
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.KtorLauncher

fun main() {
  val usersHandler: UsersHandler = ExposedUsersHandler
  val passwordHashing: PasswordHashing = JBCryptPasswordHashing
  val jwtGenerator: JwtGenerator = KtorJwtGenerator

  val userUsecases = UserUsecases(
    usersHandler,
    passwordHashing,
    jwtGenerator
  )

  ExposedInitializer.initialize()
  KtorLauncher(userUsecases, jwtGenerator).launch()
}