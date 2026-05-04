package com.lucasalfare.flpoint.server.domain.model

import kotlinx.datetime.TimeZone

data class User(
  val id: Int,
  val name: String,
  val email: String,
  val hashedPassword: String,
  val timeZone: TimeZone,
  val isAdmin: Boolean
) {
  fun toUserDto() = UserDTO(
    id, name, email, timeZone, isAdmin
  )
}
