package com.lucasalfare.flpoint.server.c_infra.data.exposed

import com.lucasalfare.flpoint.server.a_domain.TimeRegistrationsHandler
import kotlinx.datetime.LocalDateTime

object ExposedTimeRegistrationsHandler : TimeRegistrationsHandler {
  override suspend fun create(relatedUser: Int, dateTime: LocalDateTime): Result<Int> {
    TODO("Not yet implemented")
  }
}