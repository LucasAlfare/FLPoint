package com.lucasalfare.flpoint.server.a_domain.model.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ClockInRequestDTO(
  val userId: Int,
  val timestamp: LocalDateTime
) {

  init {
    // TODO: validate here only integrity of fields.
    // TODO: meaning of then will be validated in use case.
  }
}