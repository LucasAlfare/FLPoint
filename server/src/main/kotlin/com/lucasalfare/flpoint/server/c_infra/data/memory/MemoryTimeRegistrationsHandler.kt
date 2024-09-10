package com.lucasalfare.flpoint.server.c_infra.data.memory

import com.lucasalfare.flpoint.server.a_domain.TimeRegistrationsHandler
import com.lucasalfare.flpoint.server.a_domain.model.TimeRegistration
import kotlinx.datetime.LocalDateTime

object MemoryTimeRegistrationsHandler : TimeRegistrationsHandler {

  private val timeRegistrations = mutableListOf<TimeRegistration>()

  override suspend fun create(relatedUser: Int, dateTime: LocalDateTime): Result<Int> {
    val nextId = timeRegistrations.size + 1
    timeRegistrations += TimeRegistration(nextId, relatedUser, dateTime)
    return Result.success(nextId)
  }

  override suspend fun get(relatedUser: Int) =
    Result.success(
      timeRegistrations
        .filter { it.relatedUserId == relatedUser }
        .sortedBy { it.timestamp }
    )
}