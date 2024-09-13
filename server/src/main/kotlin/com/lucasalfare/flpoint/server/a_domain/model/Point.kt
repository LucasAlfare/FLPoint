package com.lucasalfare.flpoint.server.a_domain.model

import kotlinx.datetime.Instant

data class Point(
  val id: Int,
  val relatedUserId: Int,
  val timestamp: Instant
)