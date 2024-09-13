package com.lucasalfare.flpoint.server.a_domain.model.dto

import com.lucasalfare.flpoint.server.a_domain.Constants
import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import com.lucasalfare.flpoint.server.a_domain.model.ValidationError
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserDTO(
  val name: String,
  val email: String,
  val plainPassword: String,
  val targetRole: UserRole
) {

  init {
    if (
      name.isEmpty() ||
      email.isEmpty() ||
      !email.matches(Constants.EMAIL_ADDRESS_PATTERN.toRegex()) ||
      plainPassword.isEmpty() ||
      plainPassword.length < 4 // TODO: "4" is a rule
    ) {
      throw ValidationError()
    }
  }
}