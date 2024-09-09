package com.lucasalfare.flpoint.server.a_domain.model

import kotlinx.datetime.LocalDateTime

data class TimeRegistration(
  val id: Int,
  val relatedUserId: Int,
  val timestamp: LocalDateTime
)