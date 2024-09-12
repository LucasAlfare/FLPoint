package com.lucasalfare.flpoint.server.c_infra.data.memory

import com.lucasalfare.flpoint.server.a_domain.PointsHandler
import com.lucasalfare.flpoint.server.a_domain.model.Point
import kotlinx.datetime.Instant

object MemoryPointsHandler : PointsHandler {

  private val points = mutableListOf<Point>()

  override suspend fun create(relatedUser: Int, timestamp: Instant): Result<Int> {
    val nextId = points.size + 1
    points += Point(nextId, relatedUser, timestamp)
    return Result.success(nextId)
  }

  override suspend fun get(relatedUser: Int) =
    Result.success(
      points
        .filter { it.relatedUserId == relatedUser }
        .sortedBy { it.instant }
    )

  override suspend fun clear(): Result<Boolean> {
    points.clear()
    return Result.success(true)
  }
}