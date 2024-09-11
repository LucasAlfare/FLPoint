package com.lucasalfare.flpoint.server.b_usecase.rule

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.minutes

object PointUsecasesRules {

  fun allPasses(last: LocalDateTime, check: LocalDateTime): Boolean {
    return isAtMax1MinuteAwayFromServer(check) && passedAtLeast30MinutesFromLast(last, check)
  }

  fun isAtMax1MinuteAwayFromServer(check: LocalDateTime): Boolean {
    val nowMs: Long = Clock.System.now().toEpochMilliseconds()
    val checkMs: Long = check.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    // the check must be, at max, 1 second after now
    return checkMs <= (nowMs + 1000L)
  }

  fun passedAtLeast30MinutesFromLast(last: LocalDateTime, check: LocalDateTime): Boolean {
    val l = last.toInstant(TimeZone.UTC)
    val c = check.toInstant(TimeZone.UTC)

    // interval between last and check must be, at least, 30 minutes
    return c - l >= 30.minutes
  }
}