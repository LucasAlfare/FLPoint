package com.lucasalfare.flpoint.server.b_usecase

import com.lucasalfare.flpoint.server.a_domain.JwtGenerator
import com.lucasalfare.flpoint.server.a_domain.PasswordHashing
import com.lucasalfare.flpoint.server.a_domain.UsersHandler
import com.lucasalfare.flpoint.server.a_domain.model.LoginError
import com.lucasalfare.flpoint.server.a_domain.model.dto.BasicCredentialsDTO
import com.lucasalfare.flpoint.server.a_domain.model.dto.CreateUserDTO

class UserUsecases(
  var usersHandler: UsersHandler,
  var passwordHashing: PasswordHashing,
  var jwtGenerator: JwtGenerator
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

    return jwtGenerator.generate(user.email)
  }
}