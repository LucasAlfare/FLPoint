package com.lucasalfare.flpoint.server.a_domain.model

/**
 * Data class representing a user in the system.
 *
 * @property id Unique identifier for the user.
 * @property name Full name of the user.
 * @property email Email address of the user, used for communication and login.
 * @property hashedPassword Encrypted password for authentication.
 * @property role The role assigned to the user, defining their level of access and permissions.
 */
data class User(
  val id: Int,
  val name: String,
  val email: String,
  val hashedPassword: String,
  val role: UserRole
)