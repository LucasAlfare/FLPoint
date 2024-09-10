package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes

import com.lucasalfare.flpoint.server.a_domain.model.LoginError
import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.protectedForAdminRoute() {
  authenticate("flpoint-jwt-auth") {
    get("/protected-for-admin") {
      val principal = call.principal<JWTPrincipal>()

      val role = enumValueOf<UserRole>(
        principal?.payload?.getClaim("role")?.toString()?.replace("\"", "") ?: UserRole.Standard.name
      )

      if (role == UserRole.Admin) {
        return@get call.respond(
          HttpStatusCode.OK,
          "You are an admin and was authorized to access this route"
        )
      } else {
        throw LoginError()
      }
    }
  }
}