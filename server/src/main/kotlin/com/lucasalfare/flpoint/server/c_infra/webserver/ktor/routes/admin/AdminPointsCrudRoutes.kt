package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.admin

import com.lucasalfare.flpoint.server.b_usecase.PointUsecases
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.handleAsAuthenticatedAdmin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.adminPointsCrudRoutes(pointUsecases: PointUsecases) {
  authenticate("flpoint-jwt-auth") {
    get("/admin/points") {
      return@get handleAsAuthenticatedAdmin {
        val result = pointUsecases.getAllPointsOfAllUsers()
        call.respond(HttpStatusCode.OK, result.getOrThrow())
      }
    }

    delete("/admin/points/{id}") {
      return@delete handleAsAuthenticatedAdmin {
        val id = call.parameters["id"]!!.toInt()
        if (pointUsecases.deletePoint(id)) {
          call.respond(HttpStatusCode.OK)
        }
      }
    }
  }
}