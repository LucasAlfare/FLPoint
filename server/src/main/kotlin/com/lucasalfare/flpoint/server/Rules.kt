package com.lucasalfare.flpoint.server

@Suppress("MemberVisibilityCanBePrivate")
class Rules {

  companion object {

    const val ONE_SECOND = 1000
    const val ONE_MINUTE = 60 * ONE_SECOND
    const val ONE_HOUR = 60 * ONE_MINUTE
    const val ONE_DAY = 24 * ONE_HOUR

    /**
     * The time between the last stored registration and the
     * actual requested must be, at least, equals or higher
     * than this value.
     *
     * This time is measured in milliseconds.
     *
     */
    const val DEFAULT_MIN_REGISTRATION_INTERVAL = 30 * ONE_MINUTE

    /**
     * If the difference between the received registration time
     * and the current clock time exceeds this limit, then the
     * registration will be not allowed.
     *
     * Must be set to a good value, in order to save users with
     * slow connections.
     *
     * This time is measured in milliseconds.
     */
    const val DEFAULT_MAX_REGISTRATION_DELAY = 2 * ONE_MINUTE
  }
}