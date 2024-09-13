package com.lucasalfare.flpoint.server.a_domain.model

/**
 * Enum class representing the different roles a user can have in the system.
 *
 * @property Standard Represents a standard user with basic access and permissions.
 * @property Admin Represents an administrator with elevated privileges and access to additional functionalities.
 */
enum class UserRole {
  Standard,
  Admin
}