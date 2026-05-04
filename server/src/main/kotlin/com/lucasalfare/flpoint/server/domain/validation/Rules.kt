package com.lucasalfare.flpoint.server.domain.validation

import com.lucasalfare.flpoint.server.shared.RuleViolatedError
import com.lucasalfare.flpoint.server.shared.ValidationError
import com.lucasalfare.flpoint.server.shared.Constants
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

fun instantIsAtLeast10SecondsAwayFromLast(check: Instant, lastInstant: Instant): Boolean =
  check - lastInstant >= 10.seconds

fun validateName(name: String) {
  if (name.isBlank()) throw ValidationError("Name cannot be empty")
  if (name.length < 2) throw ValidationError("Name must have at least 2 characters")
}

fun validateEmail(email: String) {
  val emailRegex = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$".toRegex()
  if (!email.matches(emailRegex)) throw ValidationError("Invalid email format")
}

fun validatePassword(password: String) {
  if (password.length < Constants.DEFAULT_MIN_PASSWORD_LENGTH)
    throw ValidationError("Password must have at least ${Constants.DEFAULT_MIN_PASSWORD_LENGTH} characters")
}
