package com.lucasalfare.flpoint.server.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDTO(
  val userId: Long,
  val jwt: String
)