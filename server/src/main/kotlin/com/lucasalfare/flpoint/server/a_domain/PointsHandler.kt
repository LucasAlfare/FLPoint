package com.lucasalfare.flpoint.server.a_domain

import com.lucasalfare.flpoint.server.a_domain.model.Point
import kotlinx.datetime.Instant

interface PointsHandler {

  suspend fun create(relatedUser: Int, timestamp: Instant): Result<Int>

  suspend fun get(): Result<List<Point>>

  suspend fun get(relatedUser: Int): Result<List<Point>>

  suspend fun delete(id: Int): Boolean

  suspend fun clear(): Result<Boolean>
}