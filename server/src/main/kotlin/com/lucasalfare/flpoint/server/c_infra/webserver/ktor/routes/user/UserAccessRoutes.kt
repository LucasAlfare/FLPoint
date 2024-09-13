package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.user

import com.lucasalfare.flpoint.server.a_domain.model.dto.BasicCredentialsDTO
import com.lucasalfare.flpoint.server.a_domain.model.dto.CreateUserDTO
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// routes that doesn't requires authentication to access
fun Routing.userAccessRoutes(userUsecases: UserUsecases) {
  post("/login") {
    val dto = call.receive<BasicCredentialsDTO>()
    val jwt = userUsecases.loginUser(dto)
    call.respond(HttpStatusCode.OK, jwt)
  }

  post("/register") {
    val dto = call.receive<CreateUserDTO>()
    val result = userUsecases.createUser(dto)
    call.respond(HttpStatusCode.Created, result.getOrNull()!!)
  }
}