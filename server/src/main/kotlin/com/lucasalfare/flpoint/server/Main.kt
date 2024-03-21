package com.lucasalfare.flpoint.server

import com.lucasalfare.flpoint.server.data.AppDB
import com.lucasalfare.flpoint.server.data.services.Users
import com.lucasalfare.flpoint.server.models.errors.AppResult
import com.lucasalfare.flpoint.server.models.errors.RequestError
import com.lucasalfare.flpoint.server.routes.login
import com.lucasalfare.flpoint.server.routes.protected
import com.lucasalfare.flpoint.server.routes.signup
import com.lucasalfare.flpoint.server.routes.timeRegistration
import com.lucasalfare.flpoint.server.security.DEFAULT_JWT_CONFIG
import com.lucasalfare.flpoint.server.security.MyJwtConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
  AppDB.initialize(
    username = "FLPointUsername",
    password = "FLPointPassword"
  )

  embeddedServer(Netty, 3000) {
    configureAuthentication()
    configureSerialization()
    configureRouting()
  }.start(true)
}

fun Application.configureRouting() {
  routing {
    /*
    - The client sends a JSON with login and password (original, not hashed);
    - The server responds with:
      - The code OK and with the just created user ID ([Long]) when everything works;
      - The code NotAcceptable and the error messages when something goes wrong.

    Example request:
    curl -d '{"login":"qwerty", "password":"hehehehe"}' -H "Content-Type: application/json" -X POST http://localhost:3000/flpoint/users/signup
     */
    signup()

    /*
    - The client sends a JSON with login and password (original, not hashed);
    - The server responds with:
      - The code OK and a JWT when login succeeds;
      - The code NotAcceptable when [login || password] doesn't match;
      - The code NotFound when login doesn't exist

     Example request:

     curl -d '{"login":"qwerty", "password":"hehehehe"}' -H "Content-Type: application/json" -X POST http://localhost:3000/flpoint/users/login
     */
    login()

    /*
    curl -d '{"dateTime": 8127681276192}' -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImlzcyI6IkZMUG9pbnQiLCJ1c2VyX2lkIjoxLCJleHAiOjE3MTA0NzE4NzJ9.zFKaIaLXa497fwcDJedtL8Y74-OYlzGoEBLZ1ZVXla8" -X POST http://localhost:3000/flpoint/users/1/time_registration
     */
    timeRegistration()

    /*
    curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImlzcyI6IkZMUG9pbnQiLCJ1c2VyX2lkIjoxLCJleHAiOjE3MTA0NzE4NzJ9.zFKaIaLXa497fwcDJedtL8Y74-OYlzGoEBLZ1ZVXla8" -X GET http://localhost:3000/flpoint/users/1/protected
     */
    protected()
  }
}

fun Application.configureSerialization() {
  install(ContentNegotiation) {
    json()
  }
}

fun Application.configureAuthentication() {
  install(Authentication) {
    jwt(DEFAULT_JWT_CONFIG) {
      verifier(MyJwtConfig.getVerifier())
      realm = "FLPoint"

      /**
       * In this validation we search for the User ID that should be encrypted inside
       * the incoming JWT payload and see if it corresponds to something that exists
       * in the application Database. If so, then the token is valid.
       *
       * In other words, we expect receive a claim called "user_id" [Long] inside the received
       * JWT.
       */
      validate { credential ->
        when (Users.getUserById(credential.payload.getClaim("user_id").asLong())) {
          is AppResult.Success -> JWTPrincipal(credential.payload)
          else -> null
        }
      }

      /**
       * If the incoming token verification fails, we return an Unauthorized response
       * to the client, suggesting to retry to log in later.
       */
      challenge { _, _ ->
        call.respond(HttpStatusCode.Unauthorized, "Not allowed to log in now. Try again later.")
      }
    }
  }
}

fun Throwable.toErrorResponseString(): String {
  val t = this
  return buildString {
    if (t.message != null) append("Message:\n\t${t.message}\n")
    if (t.cause != null) append("Cause:\n\t${t.cause}\n")
  }
}

suspend fun respondError(call: ApplicationCall) {
  val theError = AppResult.Failure<Unit, RequestError>(RequestError.BadUrl)
  call.respond(theError.statusCode, theError.error)
}