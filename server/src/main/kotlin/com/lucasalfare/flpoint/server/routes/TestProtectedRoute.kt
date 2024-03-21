package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.data.services.Users
import com.lucasalfare.flpoint.server.models.errors.AppResult.Failure
import com.lucasalfare.flpoint.server.models.errors.AppResult.Success
import com.lucasalfare.flpoint.server.security.DEFAULT_JWT_CONFIG
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.protected() {
  authenticate(DEFAULT_JWT_CONFIG) {
    get("flpoint/users/{id}/protected") {
      try {
        val id = call.parameters["id"]?.toLong() ?: return@get respondError(call)
        when (val result = Users.getUserById(id)) {
          is Success -> {
            return@get call.respond(
              result.statusCode,
              "This is a protected route to see protected data, which is:\n\t${result.data}"
            )
          }

          is Failure -> {
            return@get call.respond(
              result.statusCode,
              "You are in a protected route but the protected data was not found in the database.\n\t${result.error}"
            )
          }

          else -> {}
        }
      } catch (e: Exception) {
        return@get respondError(call)
      }
    }
  }
}