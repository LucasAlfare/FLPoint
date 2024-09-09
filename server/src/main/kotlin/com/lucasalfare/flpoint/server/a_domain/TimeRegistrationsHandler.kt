package com.lucasalfare.flpoint.server.a_domain

import kotlinx.datetime.LocalDateTime

interface TimeRegistrationsHandler {

  suspend fun create(relatedUser: Int, dateTime: LocalDateTime): Result<Int>
}