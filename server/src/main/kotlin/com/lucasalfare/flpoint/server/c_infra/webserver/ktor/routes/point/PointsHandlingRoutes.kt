package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.point

import com.lucasalfare.flpoint.server.a_domain.model.dto.CreatePointRequestDTO
import com.lucasalfare.flpoint.server.b_usecase.PointUsecases
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.pointsHandlingRoutes(pointUsecases: PointUsecases) {
  authenticate("flpoint-jwt-auth") {
    post("/point") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal?.payload?.getClaim("id")?.asInt() ?: -1
      val req = call.receive<CreatePointRequestDTO>()
      val result = pointUsecases.createTimeRegistration(userId, req)
      return@post call.respond(HttpStatusCode.Created, result.getOrNull()!!)
    }

    get("/points") {
      val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
      val result = pointUsecases.getAllUserPoints(userId)
      return@get call.respond(HttpStatusCode.OK, result)
    }
  }
}