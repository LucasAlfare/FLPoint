package com.lucasalfare.flpoint.server.a_domain.model.dto

import com.lucasalfare.flpoint.server.a_domain.model.ValidationError
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) for creating a new point (record).
 *
 * This DTO is used to encapsulate the necessary information to create a new point record for a user.
 *
 * @property timestamp The exact time when the point was recorded, represented as an `Instant`.
 */
@Serializable
data class CreatePointRequestDTO(
  val timestamp: Instant
) {

  init {
    val now = Clock.System.now()
    if (timestamp > now) {
      throw ValidationError("Timestamp cannot be in the future")
    }
  }
}