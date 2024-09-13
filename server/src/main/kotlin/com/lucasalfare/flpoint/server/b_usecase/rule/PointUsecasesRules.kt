package com.lucasalfare.flpoint.server.b_usecase.rule

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Object containing utility functions for validating point use case rules.
 *
 * Provides methods to check if a point record's timestamp is within a valid time range
 * and to verify if a certain amount of time has passed since the last recorded point.
 */
object PointUsecasesRules {

  /**
   * Checks if a given timestamp is within a valid time range relative to the current time.
   *
   * The valid time range is defined as the current time minus 10 seconds to the current time plus 1 second.
   *
   * @param check The timestamp to be checked.
   * @return `true` if the timestamp is within the valid time range, `false` otherwise.
   */
  fun isWithinValidTimeRange(check: Instant): Boolean {
    val now = Clock.System.now()
    val lowerBound = now - 10.seconds
    val higherBound = now + 1.seconds
    return check in lowerBound..higherBound
  }

  /**
   * Verifies if at least 30 minutes have passed since the last recorded timestamp.
   *
   * @param last The timestamp of the last recorded point.
   * @param check The timestamp to check against the last recorded point.
   * @return `true` if at least 30 minutes have passed since the last timestamp, `false` otherwise.
   */
  fun passedAtLeast30MinFromLast(last: Instant, check: Instant): Boolean {
    return check - last >= 30.minutes
  }
}