package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.data.services.Users
import com.lucasalfare.flpoint.server.models.dto.Credentials
import com.lucasalfare.flpoint.server.models.errors.AppResult.Failure
import com.lucasalfare.flpoint.server.models.errors.AppResult.Success
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signup() {
  post("/flpoint/users/signup") {
    try {
      val credentials = call.receive<Credentials>()
      when (val result = Users.createUser(credentials)) {
        is Success -> {
          return@post call.respond(result.statusCode, result.data)
        }

        is Failure -> {
          return@post call.respond(result.statusCode, result.error)
        }

        else -> {}
      }
    } catch (e: Exception) {
      return@post respondError(call)
    }
  }
}