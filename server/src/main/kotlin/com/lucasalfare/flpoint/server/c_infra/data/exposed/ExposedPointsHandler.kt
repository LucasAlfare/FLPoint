package com.lucasalfare.flpoint.server.c_infra.data.exposed

import com.lucasalfare.flpoint.server.a_domain.PointsHandler
import com.lucasalfare.flpoint.server.a_domain.model.Point
import kotlinx.datetime.LocalDateTime

object ExposedPointsHandler : PointsHandler {
  override suspend fun create(relatedUser: Int, dateTime: LocalDateTime): Result<Int> {
    TODO("Not yet implemented")
  }

  override suspend fun get(relatedUser: Int): Result<List<Point>> {
    TODO("Not yet implemented")
  }
}