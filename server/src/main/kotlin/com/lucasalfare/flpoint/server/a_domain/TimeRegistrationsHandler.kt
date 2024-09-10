package com.lucasalfare.flpoint.server.a_domain

import com.lucasalfare.flpoint.server.a_domain.model.TimeRegistration
import kotlinx.datetime.LocalDateTime

interface TimeRegistrationsHandler {

  suspend fun create(relatedUser: Int, dateTime: LocalDateTime): Result<Int>

  suspend fun get(relatedUser: Int): Result<List<TimeRegistration>>
}