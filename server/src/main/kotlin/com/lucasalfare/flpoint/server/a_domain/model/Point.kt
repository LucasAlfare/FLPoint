package com.lucasalfare.flpoint.server.a_domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Point(
  val id: Int,
  val relatedUserId: Int,
  val timestamp: Instant
)