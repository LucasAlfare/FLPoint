package com.lucasalfare.flpoint.server.domain.model

import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import com.lucasalfare.flpoint.server.domain.validation.validateName
import com.lucasalfare.flpoint.server.domain.validation.validateEmail
import com.lucasalfare.flpoint.server.domain.validation.validatePassword

@Serializable
data class CreateUserRequestDTO(
  val name: String,
  val email: String,
  val plainPassword: String,
  val timeZone: TimeZone = TimeZone.UTC
) {
  init {
    validateName(name)
    validateEmail(email)
    validatePassword(plainPassword)
  }
}

@Serializable
data class UpdateUserPasswordRequestDTO(
  val currentPlainPassword: String,
  val newPlainPassword: String,
) {

  init {
    validatePassword(currentPlainPassword)
    validatePassword(newPlainPassword)
  }
}

@Serializable
data class LoginResponseDTO(
  val jwt: String,
  val userDTO: UserDTO
)

@Serializable
data class CredentialsDTO(
  val email: String,
  val plainPassword: String
) {
  init {
    validateEmail(email)
    validatePassword(plainPassword)
  }
}

@Serializable
data class UserDTO(
  val id: Int,
  val name: String,
  val email: String,
  val timeZone: TimeZone,
  val isAdmin: Boolean
)

@Serializable
data class PointDTO(
  val id: Int,
  val relatedUserId: Int,
  val instant: Instant
)
