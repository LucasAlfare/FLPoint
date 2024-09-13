package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration

import com.lucasalfare.flpoint.server.c_infra.security.jwt.ktor.KtorJwtGenerator
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*

/**
 * Configures JWT authentication for the Ktor application.
 * This installs the JWT authentication feature and sets up token validation,
 * including checking the claims in the JWT payload and responding appropriately to invalid tokens.
 */
fun Application.authenticationConfiguration() {
  val appRef = this

  install(Authentication) {
    jwt("flpoint-jwt-auth") {
      realm = "JWT_AUTH_REALM"
      verifier(KtorJwtGenerator.verifier)

      validate { credential ->
        // Extract and validate claims from the JWT token
        val id = credential.payload.getClaim("id").asInt()
        val login = credential.payload.getClaim("login").asString()
        val role = credential.payload.getClaim("role").asString()

        // Check if all necessary claims are present and valid
        if (
          id != null &&
          login.isNotBlank() &&
          role.isNotBlank()
        ) {
          // Return a JWTPrincipal if claims are valid
          JWTPrincipal(credential.payload)
        } else {
          // Return null if any claim is empty or invalid
          null
        }
      }

      challenge { _, _ ->
        // Log a warning when an invalid JWT token is detected
        appRef.log.warn(
          "Detected attempt to access an authenticated route with a bad JWT Token from ${call.request.origin.remoteHost}"
        )
        // Respond with an error message and Unauthorized status
        call.respond(HttpStatusCode.Unauthorized, "Unable to access the system: Unauthorized!")
      }
    }
  }
}