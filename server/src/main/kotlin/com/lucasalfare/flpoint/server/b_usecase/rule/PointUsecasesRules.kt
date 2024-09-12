package com.lucasalfare.flpoint.server.b_usecase.rule

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.minutes

object PointUsecasesRules {

  fun allPasses(last: LocalDateTime, check: LocalDateTime): Boolean {
    return isWithinValidTimeRange(check) && passedAtLeast30MinutesFromLast(last, check)
  }

  fun isWithinValidTimeRange(check: LocalDateTime): Boolean {
    val nowMs: Long = Clock.System.now().toEpochMilliseconds()
    val checkMs = check.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    // Check if the time is no more than 1 second in the future and no more than 10 seconds in the past
    return checkMs <= nowMs + 1000L && checkMs >= nowMs - 10_000L
  }

  fun passedAtLeast30MinutesFromLast(last: LocalDateTime, check: LocalDateTime): Boolean {
    val l = last.toInstant(TimeZone.currentSystemDefault())
    val c = check.toInstant(TimeZone.currentSystemDefault())

    // interval between last and check must be, at least, 30 minutes
    return c - l >= 30.minutes
  }
}