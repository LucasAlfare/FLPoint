package com.lucasalfare.flpoint.server.a_domain.model.dto

import com.lucasalfare.flpoint.server.a_domain.Constants
import com.lucasalfare.flpoint.server.a_domain.model.ValidationError
import kotlinx.serialization.Serializable

@Serializable
data class BasicCredentialsDTO(
  val email: String,
  val plainPassword: String
) {

  init {
    if (
      email.isEmpty() ||
      !email.matches(Constants.EMAIL_ADDRESS_PATTERN.toRegex()) ||
      plainPassword.isEmpty() ||
      plainPassword.length < 4
    ) {
      throw ValidationError()
    }
  }
}