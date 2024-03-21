package com.lucasalfare.flpoint.server.models.errors

import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Default markup interface for defining an error
 */
interface AppError

/**
 * Default markup interface for defining a result, with generic Data and Error.
 * This wraps two entities: Success and Failure.
 *
 * Success carries only data. Failure carries only error.
 *
 * The error referenced here is an instance of [AppError].
 */
interface AppResult<out D, out E : AppError> {

  @Serializable
  data class Success<out D, out E : AppError>(
    @Contextual val data: D,
    @Contextual val statusCode: HttpStatusCode = HttpStatusCode.OK
  ) : AppResult<D, E>

  @Serializable
  data class Failure<out D, out E : AppError>(
    @Contextual val error: E,
    @Contextual val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError
  ) : AppResult<D, E>
}