package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.dummy

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.protectedRoute() {
  authenticate("flpoint-jwt-auth") {
    get("/protected") {
      return@get call.respond(
        HttpStatusCode.OK,
        "You have are authorized to access this route"
      )
    }
  }
}