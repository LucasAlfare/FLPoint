package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.data.models.dto.Credentials
import com.lucasalfare.flpoint.server.data.service.Users
import com.lucasalfare.flpoint.server.toErrorResponseString
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signup() {
  post("/flpoint/user") {
    runCatching {
      val credentials = call.receive<Credentials>()
      val result = Users.createUser(credentials.login, credentials.password)
      return@post call.respond(result.code, result.data ?: "")
    }.onFailure {
      return@post call.respond(HttpStatusCode.NotAcceptable, it.toErrorResponseString())
    }
  }
}