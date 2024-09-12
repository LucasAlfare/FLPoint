package com.lucasalfare.flpoint.server.b_usecase.rule

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

object PointUsecasesRules {

  fun allPasses(last: Instant, check: Instant): Boolean {
    return isWithinValidTimeRange(check) && passedAtLeast30MinutesFromLast(last, check)
  }

  fun isWithinValidTimeRange(check: Instant): Boolean {
    val nowMs: Long = Clock.System.now().toEpochMilliseconds()
    val checkMs = check.toEpochMilliseconds()

    // Check if the time is no more than 1 second in the future and no more than 10 seconds in the past
    return checkMs <= nowMs + 1000L && checkMs >= nowMs - 10_000L
  }

  fun passedAtLeast30MinutesFromLast(last: Instant, check: Instant): Boolean {
    // interval between last and check must be, at least, 30 minutes
    return check - last >= 30.minutes
  }
}