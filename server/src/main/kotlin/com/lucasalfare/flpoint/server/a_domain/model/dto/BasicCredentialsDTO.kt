package com.lucasalfare.flpoint.server.a_domain.model.dto

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
      plainPassword.isEmpty() ||
      plainPassword.length < 4 // TODO: "4" is a rule
    ) {
      throw ValidationError()
    }
  }
}