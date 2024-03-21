package com.lucasalfare.flpoint.server

class Rules {

  companion object {

    /**
     * The time between the last stored registration and the
     * actual requested must be, at least, equals or higher
     * than this value.
     *
     * This time is measured in milliseconds.
     *
     */
    const val DEFAULT_MIN_REGISTRATION_INTERVAL = 1 * (1000)

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
    const val DEFAULT_MAX_REGISTRATION_DELAY = 5 * (1000)
  }
}