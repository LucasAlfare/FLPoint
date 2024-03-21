package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.data.services.validators.UserExistenceByCredentialsValidator
import com.lucasalfare.flpoint.server.models.dto.Credentials
import com.lucasalfare.flpoint.server.models.errors.AppResult
import com.lucasalfare.flpoint.server.toErrorResponseString
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.login() {
  post("/flpoint/users/login") {
    runCatching {
      val credentials = call.receive<Credentials>()

      when (val result = UserExistenceByCredentialsValidator(credentials).validate()) {
        is AppResult.Success -> {
          return@post call.respond(result.statusCode, result.data)
        }

        is AppResult.Failure -> {
          return@post call.respond(result.statusCode, result.error)
        }

        else -> {}
      }
    }.onFailure {
      // tries to catch internal errors
      return@post call.respond(HttpStatusCode.InternalServerError, it.toErrorResponseString())
    }
  }
}