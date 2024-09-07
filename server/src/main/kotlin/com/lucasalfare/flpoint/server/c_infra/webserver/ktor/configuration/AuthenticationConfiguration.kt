package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration

import com.lucasalfare.flpoint.server.c_infra.security.jwt.ktor.KtorJwtGenerator
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*

fun Application.authenticationConfiguration() {
  val appRef = this

  install(Authentication) {
    jwt("flpoint-jwt-auth") {
      realm = "JWT_AUTH_REALM"
      verifier(KtorJwtGenerator.verifier)

      validate { credential ->
        val login = credential.payload.getClaim("login").asString()
        val role = credential.payload.getClaim("role").asString()

        // Valida se o login e a role existem no token
        if (login.isNotBlank() && role.isNotBlank()) {
          JWTPrincipal(credential.payload)
        } else {
          null // Token inválido se algum claim estiver vazio
        }
      }

      challenge { _, _ ->
        appRef.log.warn(
          "Detected attempt of access an authenticated route with bad JWT Token from ${call.request.origin.remoteHost}"
        )
        call.respond(HttpStatusCode.Unauthorized, "Unable to access the system: Unauthorized!")
      }
    }
  }
}