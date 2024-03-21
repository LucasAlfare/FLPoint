package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.models.errors.AppResult
import com.lucasalfare.flpoint.server.models.errors.RequestError
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun respondError(call: ApplicationCall) {
  val theError = AppResult.Failure<Unit, RequestError>(RequestError.BadRequest)
  call.respond(theError.statusCode, theError.error)
}