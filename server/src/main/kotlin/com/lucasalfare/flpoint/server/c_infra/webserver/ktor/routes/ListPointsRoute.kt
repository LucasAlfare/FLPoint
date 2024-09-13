package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes

import com.lucasalfare.flpoint.server.b_usecase.PointUsecases
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.listPointsRoute(pointUsecases: PointUsecases) {
  authenticate("flpoint-jwt-auth") {
    get("/points") {
      val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
      val result = pointUsecases.getAllUserPoints(userId)
      return@get call.respond(HttpStatusCode.OK, result)
    }
  }
}