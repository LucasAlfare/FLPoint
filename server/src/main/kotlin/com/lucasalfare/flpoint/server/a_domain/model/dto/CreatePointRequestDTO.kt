package com.lucasalfare.flpoint.server.a_domain.model.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CreatePointRequestDTO(
  val timestamp: Instant
)