@file:Suppress("MemberVisibilityCanBePrivate")

package com.lucasalfare.flpoint.server.b_usecase

import com.lucasalfare.flpoint.server.a_domain.PasswordHashing
import com.lucasalfare.flpoint.server.a_domain.UsersHandler
import com.lucasalfare.flpoint.server.a_domain.model.LoginError
import com.lucasalfare.flpoint.server.a_domain.model.dto.BasicCredentialsDTO
import com.lucasalfare.flpoint.server.a_domain.model.dto.CreateUserDTO
import com.lucasalfare.flpoint.server.c_infra.security.jwt.ktor.KtorJwtGenerator

class UserUsecases(
  var usersHandler: UsersHandler,
  var passwordHashing: PasswordHashing
) {

  suspend fun createUser(createUserDTO: CreateUserDTO) = usersHandler.create(
    createUserDTO.name,
    createUserDTO.email,
    passwordHashing.hashed(plain = createUserDTO.plainPassword),
    createUserDTO.targetRole
  )

  suspend fun loginUser(basicCredentialsDTO: BasicCredentialsDTO): String {
    val result = usersHandler.get(basicCredentialsDTO.email)
    if (result.isFailure) throw LoginError()

    val user = result.getOrNull()!!
    val passwordMatches = passwordHashing.plainMatchHashed(basicCredentialsDTO.plainPassword, user.hashedPassword)
    if (!passwordMatches) throw LoginError()

    return KtorJwtGenerator.generate(user.email, user.role)
  }
}