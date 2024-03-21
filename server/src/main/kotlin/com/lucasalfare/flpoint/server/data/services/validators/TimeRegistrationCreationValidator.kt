package com.lucasalfare.flpoint.server.data.services.validators

import com.lucasalfare.flpoint.server.models.errors.AppResult
import com.lucasalfare.flpoint.server.models.errors.BusinessError

data class TimeRegistrationCreationValidator(
  val actualValidationDateTime: Long,
  val targetUserId: Long
) : Validator<Unit, BusinessError> {
  override suspend fun validate(): AppResult<Unit, BusinessError> {
//    when (val lastRegistration = TimeRegistrations.getLastRegistrationByUserId(targetUserId)) {
//      is AppResult.Success -> {
//        // the actual time must be higher than the last registration + default_interval
//        if ((lastRegistration.data.dateTime + Rules.DEFAULT_MIN_REGISTRATION_INTERVAL) >= actualValidationDateTime) {
//          return AppResult.Success(Unit)
//        }
//      }
//    }

    return AppResult.Success(Unit)
  }
}