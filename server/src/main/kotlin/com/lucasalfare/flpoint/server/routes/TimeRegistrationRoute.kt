package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.data.services.TimeRegistrations
import com.lucasalfare.flpoint.server.models.TimeRegistration
import com.lucasalfare.flpoint.server.models.errors.AppResult
import com.lucasalfare.flpoint.server.security.DEFAULT_JWT_CONFIG
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.timeRegistration() {
  authenticate(DEFAULT_JWT_CONFIG) {
    post("/flpoint/users/{id}/time_registration") {
      try {
        val id = call.parameters["id"]?.toLong() ?: return@post respondError(call)
        val timeRegistration = call.receive<TimeRegistration>()
        when (val result = TimeRegistrations.createTimeRegistration(timeRegistration.dateTime, id)) {
          is AppResult.Success -> {
            return@post call.respond(result.statusCode, result.data)
          }

          is AppResult.Failure -> {
            return@post call.respond(result.statusCode, result.error)
          }

          else -> {}
        }
      } catch (e: Exception) {
        return@post respondError(call)
      }
    }
  }
}