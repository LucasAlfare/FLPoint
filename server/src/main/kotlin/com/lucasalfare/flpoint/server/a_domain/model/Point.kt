package com.lucasalfare.flpoint.server.a_domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Data class representing a point (record) associated with a user in the system.
 *
 * @property id Unique identifier for the point record.
 * @property relatedUserId Identifier of the user associated with this point record.
 * @property timestamp The exact time when the point was recorded.
 */
@Serializable
data class Point(
  val id: Int,
  val relatedUserId: Int,
  val timestamp: Instant
)