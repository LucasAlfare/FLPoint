package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes

import com.lucasalfare.flpoint.server.a_domain.model.dto.BasicCredentialsDTO
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// curl -v -H 'Content-Type: application/json' -d '{"email": "asdf@qwer.com", "plainPassword": "zxcv"}' -X POST http://localhost:7171/login
fun Routing.loginPostRoute(userUsecases: UserUsecases) {
  post("/login") {
    val dto = call.receive<BasicCredentialsDTO>()
    val jwt = userUsecases.loginUser(dto)
    call.respond(HttpStatusCode.OK, jwt)
  }
}