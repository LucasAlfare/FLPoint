package com.lucasalfare.flpoint.server.data.services.validators

import com.lucasalfare.flpoint.server.Rules
import com.lucasalfare.flpoint.server.data.services.TimeRegistrations
import com.lucasalfare.flpoint.server.models.errors.AppResult
import com.lucasalfare.flpoint.server.models.errors.BusinessError

data class TimeRegistrationCreationValidator(
  val candidateTime: Long,
  val targetUserId: Long
) : Validator<Unit, BusinessError> {
  override suspend fun validate(): AppResult<Unit, BusinessError> {

    // 1. Checks the interval between last registration and current candidate
    when (val lastRegistration = TimeRegistrations.getLastRegistrationByUserId(targetUserId)) {
      is AppResult.Success -> {
        // the actual time must be higher than the last registration + default_interval
        if ((lastRegistration.data.dateTime + Rules.DEFAULT_MIN_REGISTRATION_INTERVAL) < candidateTime) {
          return AppResult.Failure(BusinessError.NotAllowed)
        }
      }
    }

    // 2. checks if actual time candidate is too high, compared with the current server time
    if (System.currentTimeMillis() - candidateTime > Rules.DEFAULT_MAX_REGISTRATION_DELAY)
      return AppResult.Failure(BusinessError.TimeRegistrationExceedsLimitDelay)

    // 3. Checks the number of last registrations performed in a single time interval
    // TODO

    return AppResult.Success(Unit)
  }
}