package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.respondError
import com.lucasalfare.flpoint.server.security.DEFAULT_JWT_CONFIG
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.timeRegistration() {
  authenticate(DEFAULT_JWT_CONFIG) {
    post("/flpoint/users/{id}/time_registration") {
      call.parameters["id"]?.let { textedId ->
        try {
          textedId.toLong().let { id ->

          }
        } catch (e: Exception) {
          respondError(call)
        }
      }

      respondError(call)
    }
  }
}