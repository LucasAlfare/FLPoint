package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes

import com.lucasalfare.flpoint.server.a_domain.model.dto.ClockInRequestDTO
import com.lucasalfare.flpoint.server.b_usecase.TimeRegistrationsUseCases
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.clockInRoute(timeRegistrationsUseCases: TimeRegistrationsUseCases) {
  authenticate("flpoint-jwt-auth") {
    post("/clock-in") {
      val req = call.receive<ClockInRequestDTO>()
      val result = timeRegistrationsUseCases.createTimeRegistration(req)
      return@post call.respond(HttpStatusCode.Created, result.getOrNull()!!)
    }
  }
}