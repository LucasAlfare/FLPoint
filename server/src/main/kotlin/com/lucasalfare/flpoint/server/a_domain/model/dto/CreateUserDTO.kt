package com.lucasalfare.flpoint.server.a_domain.model.dto

import com.lucasalfare.flpoint.server.a_domain.Constants
import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import com.lucasalfare.flpoint.server.a_domain.model.ValidationError
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) for creating a new user.
 *
 * This DTO encapsulates the necessary information to create a new user in the system,
 * including their name, email, plain password, and desired role.
 *
 * @property name The full name of the user.
 * @property email The email address of the user, used for communication and login.
 * @property plainPassword The plain text password chosen by the user. This should be hashed before storage.
 * @property targetRole The role that the user is assigned upon creation, such as Standard or Admin.
 *
 * @throws ValidationError if any of the following conditions are met:
 * - The name is empty.
 * - The email is empty or does not match a valid format.
 * - The plain password is empty or has fewer than 4 characters.
 */
@Serializable
data class CreateUserDTO(
  val name: String,
  val email: String,
  val plainPassword: String,
  val targetRole: UserRole
) {

  /**
   * Initialization block that performs validation on the user's input.
   *
   * Validation rules:
   * - The name must not be empty.
   * - The email must not be empty and must match a valid email format.
   * - The plain password must not be empty and must be at least 4 characters long.
   *
   * @throws ValidationError If any of the validation rules are violated.
   */
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