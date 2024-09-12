package com.lucasalfare.flpoint.server.a_domain.model.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PointRequestDTO(
//  val userId: Int,
  val timestamp: Instant
)