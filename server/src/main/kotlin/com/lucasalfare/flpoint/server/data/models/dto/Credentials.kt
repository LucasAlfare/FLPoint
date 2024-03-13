package com.lucasalfare.flpoint.server.data.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(
  val login: String,
  val password: String // refers to the original password, not hashed password
) {
  init {
    require(login.isNotEmpty() || login.isNotBlank()) {
      "Invalid credentials. Blank or empty fields."
    }

    require(password.isNotEmpty() || password.isNotBlank()) {
      "Invalid credentials. Blank or empty fields."
    }

    require(login.length >= 6 && password.length >= 6) {
      "The login and password must contains at least 6 characters."
    }
  }
}