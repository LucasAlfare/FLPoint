package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.dummy

import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.handleAsAuthenticatedAdmin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.protectedForAdminRoute() {
  authenticate("flpoint-jwt-auth") {
    get("/protected-for-admin") {
      return@get handleAsAuthenticatedAdmin {
        call.respond(
          HttpStatusCode.OK,
          "You are an admin and was authorized to access this route"
        )
      }
    }
  }
}