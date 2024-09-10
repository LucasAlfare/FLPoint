package com.lucasalfare.flpoint.server.c_infra.data.exposed

import com.lucasalfare.flpoint.server.a_domain.TimeRegistrationsHandler
import com.lucasalfare.flpoint.server.a_domain.model.TimeRegistration
import kotlinx.datetime.LocalDateTime

object ExposedTimeRegistrationsHandler : TimeRegistrationsHandler {
  override suspend fun create(relatedUser: Int, dateTime: LocalDateTime): Result<Int> {
    TODO("Not yet implemented")
  }

  override suspend fun get(relatedUser: Int): Result<List<TimeRegistration>> {
    TODO("Not yet implemented")
  }
}