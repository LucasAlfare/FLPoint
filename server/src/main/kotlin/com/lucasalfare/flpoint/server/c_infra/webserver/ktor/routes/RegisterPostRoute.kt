package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes

import com.lucasalfare.flpoint.server.a_domain.model.dto.CreateUserDTO
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// curl -X http://localhost:7171 -H "Content-Type: application/json" -d '{"name": "lucas", "email": "asdf@qwer.com", "plainPassword": "zxcv", "targetRole": "Standard"}'
// curl -H 'Content-Type: application/json' -d '{"name": "lucas", "email": "asdf@qwer.com", "plainPassword": "zxcv", "targetRole": "Standard"}' -X POST http://localhost:7171/register
fun Routing.registerPostRoute(userUsecases: UserUsecases) {
  post("/register") {
    val dto = call.receive<CreateUserDTO>()
    val result = userUsecases.createUser(dto)
    call.respond(HttpStatusCode.Created, result.getOrNull()!!)
  }
}