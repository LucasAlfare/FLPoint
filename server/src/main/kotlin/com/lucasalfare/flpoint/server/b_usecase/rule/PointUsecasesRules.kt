package com.lucasalfare.flpoint.server.b_usecase.rule

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object PointUsecasesRules {

  // TODO: buggy! Fix!!!
  fun allRulesTrue(last: Instant, check: Instant): Boolean {
//    if (!isWithinValidTimeRange(check)) return false
//    if (!isAtLeast30MinFromLast(last, check)) return false
//    return true
    return isWithinValidTimeRange(check) && isAtLeast30MinFromLast(last, check)
  }

  fun isWithinValidTimeRange(check: Instant): Boolean {
    val now = Clock.System.now()

    val lowerBound = now - 10.seconds
    val higherBound = now + 1.seconds

    return check in lowerBound..higherBound
  }

  fun isAtLeast30MinFromLast(last: Instant, check: Instant): Boolean {
    // interval between last and check must be, at least, 30 minutes
    return check - last >= 30.minutes
  }
}