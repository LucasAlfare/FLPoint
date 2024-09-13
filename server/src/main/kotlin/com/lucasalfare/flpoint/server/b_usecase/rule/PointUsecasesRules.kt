package com.lucasalfare.flpoint.server.b_usecase.rule

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object PointUsecasesRules {
  fun isWithinValidTimeRange(check: Instant): Boolean {
    val now = Clock.System.now()
    val lowerBound = now - 10.seconds
    val higherBound = now + 1.seconds
    return check in lowerBound..higherBound
  }

  fun passedAtLeast30MinFromLast(last: Instant, check: Instant): Boolean {
    return check - last >= 30.minutes
  }
}