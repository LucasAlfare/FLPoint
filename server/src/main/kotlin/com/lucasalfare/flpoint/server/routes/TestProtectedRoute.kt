package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.data.services.Users
import com.lucasalfare.flpoint.server.models.errors.AppResult
import com.lucasalfare.flpoint.server.respondError
import com.lucasalfare.flpoint.server.security.DEFAULT_JWT_CONFIG
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.protected() {
  authenticate(DEFAULT_JWT_CONFIG) {
    get("flpoint/users/{id}/protected") {
      call.parameters["id"]?.let {
        when (val result = Users.getUserById(it.toLong())) {
          is AppResult.Success -> {
            return@get call.respond(result.statusCode, result.data)
          }

          is AppResult.Failure -> {
            return@get call.respond(result.statusCode, result.error)
          }

          else -> {}
        }
      }

      respondError(call)
    }
  }
}