package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.data.models.dto.Credentials
import com.lucasalfare.flpoint.server.data.models.dto.LoginResponseDTO
import com.lucasalfare.flpoint.server.data.services.Users
import com.lucasalfare.flpoint.server.security.JwtConfig
import com.lucasalfare.flpoint.server.toErrorResponseString
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.login() {
  post("/flpoint/users/login") {
    runCatching {
      val credentials = call.receive<Credentials>() // can fail
      val result = Users.validLogin(credentials.login, credentials.password)
      val searchId = result.data as Long // can fail

      val loginResponseDTO = LoginResponseDTO(searchId, JwtConfig.generateJwt(searchId))
      return@post call.respond(result.code, loginResponseDTO)
    }.onFailure {
      return@post call.respond(HttpStatusCode.InternalServerError, it.toErrorResponseString())
    }
  }
}