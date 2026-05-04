package com.lucasalfare.flpoint.server.domain.model

import kotlin.time.Instant

data class Point(
  val id: Int,
  val relatedUserId: Int,
  val instant: Instant
) {

  fun toPointDto() = PointDTO(
    id, relatedUserId, instant
  )
}
