package com.lucasalfare.flpoint.server.model

@Suppress("MemberVisibilityCanBePrivate")
class SystemRules {

  companion object {
    const val ONE_SECOND = 1000
    const val ONE_MINUTE = 60 * ONE_SECOND
    const val ONE_HOUR = 60 * ONE_MINUTE
    const val ONE_DAY = 24 * ONE_HOUR

    const val DEFAULT_MAX_POINT_REGISTRATIONS_PER_DAY = 4

    var minAuthenticationIntervalTimeMs = 3 * ONE_MINUTE
  }
}
