package com.lucasalfare.flpoint.server.a_domain.model.dto

import com.lucasalfare.flpoint.server.a_domain.Constants
import com.lucasalfare.flpoint.server.a_domain.model.ValidationError
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) representing basic login credentials.
 *
 * This DTO is used to encapsulate the email and plain password provided during authentication attempts.
 *
 * @property email The email address of the user attempting to log in.
 * @property plainPassword The plain text password associated with the user.
 * The password should be validated and hashed before storage.
 *
 * @throws ValidationError if the email is empty, invalid, or if the password is empty or too short.
 */
@Serializable
data class BasicCredentialsDTO(
  val email: String,
  val plainPassword: String
) {

  /**
   * Initialization block that performs basic validation on the email and password.
   *
   * Validation rules:
   * - The email must not be empty and must match a valid email format.
   * - The plain password must not be empty and must be at least 4 characters long.
   *
   * @throws ValidationError If any of the validation rules are violated.
   */
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