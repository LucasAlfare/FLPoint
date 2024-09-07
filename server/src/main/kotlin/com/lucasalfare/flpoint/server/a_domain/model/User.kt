package com.lucasalfare.flpoint.server.a_domain.model

data class User(
  val id: Int,
  val name: String,
  val email: String,
  val hashedPassword: String,
  val role: UserRole
)