package com.lucasalfare.flpoint.server.data.services.validators

import com.lucasalfare.flpoint.server.models.errors.AppError
import com.lucasalfare.flpoint.server.models.errors.AppResult

interface Validator<out D, out E : AppError> {

  suspend fun validate(): AppResult<D, E>
}