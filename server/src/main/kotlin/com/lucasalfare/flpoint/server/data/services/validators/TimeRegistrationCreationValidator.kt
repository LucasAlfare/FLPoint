package com.lucasalfare.flpoint.server.data.services.validators

import com.lucasalfare.flpoint.server.Rules
import com.lucasalfare.flpoint.server.data.services.TimeRegistrations

data class TimeRegistrationCreationValidator(
  val actualValidationDateTime: Long,
  val targetUserId: Long
) : Validator {

  override suspend fun isValid(): Boolean {
    val lastRegistration = TimeRegistrations.getLastRegistrationByUserId(targetUserId)
    return ((lastRegistration.data as Long) + Rules.DEFAULT_MIN_REGISTRATION_INTERVAL) >= actualValidationDateTime
  }
}