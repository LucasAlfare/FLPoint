package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes

import com.lucasalfare.flpoint.server.a_domain.model.dto.PointRequestDTO
import com.lucasalfare.flpoint.server.b_usecase.PointUsecases
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.pointRoute(pointUsecases: PointUsecases) {
  authenticate("flpoint-jwt-auth") {
    post("/point") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal?.payload?.getClaim("id")?.asInt() ?: -1
      val req = call.receive<PointRequestDTO>()
      val result = pointUsecases.createTimeRegistration(userId, req)
      return@post call.respond(HttpStatusCode.Created, result.getOrNull()!!)
    }
  }
}