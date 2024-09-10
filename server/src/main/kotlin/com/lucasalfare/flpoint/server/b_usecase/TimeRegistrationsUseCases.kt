package com.lucasalfare.flpoint.server.b_usecase

import com.lucasalfare.flpoint.server.a_domain.TimeRegistrationsHandler
import com.lucasalfare.flpoint.server.a_domain.model.UsecaseRuleError
import com.lucasalfare.flpoint.server.a_domain.model.dto.ClockInRequestDTO
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.minutes

class TimeRegistrationsUseCases(
  var timeRegistrationsHandler: TimeRegistrationsHandler
) {

  suspend fun createTimeRegistration(clockInRequestDTO: ClockInRequestDTO): Result<Int> {
    /*
    - we assume that request is pre-validated;
    - then:
      - get all registrations of request user;
      - if there are none, then just create;
      - otherwise, check rules:
        - request time is, at max, 1 minute away from server;
        - difference between last: msut be >= 30 minutes;
      - if rules ok, then create registration;
      - otherwise,
     */
    val result = timeRegistrationsHandler.get(clockInRequestDTO.userId)

    if (result.isSuccess) {
      val registrations = result.getOrNull()
      if (registrations != null) {
        if (registrations.isNotEmpty()) {
          if (
            !isAtMax1MinuteAwayFromServer(clockInRequestDTO.timestamp) ||
            !passedAtLeast30MinutesFromLast(registrations.last().timestamp, clockInRequestDTO.timestamp)
          ) {
            throw UsecaseRuleError()
          }

          return timeRegistrationsHandler.create(clockInRequestDTO.userId, clockInRequestDTO.timestamp)
        }
      }
    }

    throw UsecaseRuleError()
  }

  private fun isAtMax1MinuteAwayFromServer(check: LocalDateTime): Boolean {
    val now = Clock.System.now()
    val dateTimeInstant = check.toInstant(TimeZone.currentSystemDefault())
    val differenceInMillis = now.toEpochMilliseconds() - dateTimeInstant.toEpochMilliseconds()
    val differenceInMinutes = differenceInMillis / 1000 / 60
    return differenceInMinutes <= 1
  }

  private fun passedAtLeast30MinutesFromLast(last: LocalDateTime, current: LocalDateTime): Boolean {
    val l = last.toInstant(TimeZone.UTC)
    val c = current.toInstant(TimeZone.UTC)
    return c - l >= 30.minutes
  }
}