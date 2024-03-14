package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.data.models.TimeRegistration
import com.lucasalfare.flpoint.server.data.services.TimeRegistrations
import com.lucasalfare.flpoint.server.data.services.Users
import com.lucasalfare.flpoint.server.security.DEFAULT_JWT_CONFIG
import com.lucasalfare.flpoint.server.toErrorResponseString
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.timeRegistration() {
  authenticate(DEFAULT_JWT_CONFIG) {
    post("/flpoint/users/{id}/time_registration") {
      call.parameters["id"]?.let { textPathId ->
        runCatching {
          val urlId = textPathId.toLong()
          val searchedUser = Users.getUserById(urlId)

          if (searchedUser.code == HttpStatusCode.OK) {
            runCatching {
              val registrationTime = call.receive<TimeRegistration>()
              val registrationResult = TimeRegistrations.createTimeRegistration(registrationTime.dateTime, urlId)
              return@post call.respond(registrationResult.code, registrationResult.data ?: "")
            }.onFailure {
              return@post call.respond(HttpStatusCode.InternalServerError, it.toErrorResponseString())
            }
          } else {
            return@post call.respond(searchedUser.code, searchedUser.data ?: "")
          }
        }.onFailure {
          return@post call.respond(HttpStatusCode.BadRequest, "Wrong User ID format.")
        }
      }

      return@post call.respond(HttpStatusCode.BadRequest, "No User ID passed in URL.")
    }
  }
}