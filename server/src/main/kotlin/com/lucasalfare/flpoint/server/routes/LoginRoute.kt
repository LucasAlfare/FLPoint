package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.data.models.dto.Credentials
import com.lucasalfare.flpoint.server.data.services.Users
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
      val result = Users.validLogin(credentials.login, credentials.password)

      // TODO: generate JWT and send it in a [LoginResponseDTO] format (ID + JWT)
      return@post call.respond(result.code, result.data ?: "")
    }.onFailure {
      return@post call.respond(HttpStatusCode.InternalServerError, it.toErrorResponseString())
    }
  }
}