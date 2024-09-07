package com.lucasalfare.flpoint.server.c_infra.webserver.ktor

import com.auth0.jwt.JWTVerifier
import com.lucasalfare.flpoint.server.a_domain.JwtGenerator
import com.lucasalfare.flpoint.server.a_domain.model.DatabaseError
import com.lucasalfare.flpoint.server.a_domain.model.LoginError
import com.lucasalfare.flpoint.server.a_domain.model.ValidationError
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.loginPostRoute
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.registerPostRoute
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureAuthentication(jwtGenerator: JwtGenerator) {
  install(Authentication) {
    jwt("flpoint-jwt-auth") {
      realm = "JWT_AUTH_REALM"
      verifier(jwtGenerator.getJwtVerifier() as JWTVerifier)
      // TODO: other auth handling
    }
  }
}

fun Application.configureRouting(userUsecases: UserUsecases) {
  routing {
    registerPostRoute(userUsecases)
    loginPostRoute(userUsecases)
  }
}

fun Application.configureSerialization() {
  install(ContentNegotiation) {
    json(Json { isLenient = false })
  }
}

fun Application.configureStatusPages() {
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      println(cause.customRootCause())
      when (val root = cause.customRootCause()) {
        is ValidationError -> call.respond(HttpStatusCode.NotAcceptable, root.message ?: "ValidationError")
        is DatabaseError -> call.respond(HttpStatusCode.UnprocessableEntity, root.message ?: "DatabaseError")
        is LoginError -> call.respond(HttpStatusCode.NotAcceptable, root.message ?: "LoginError")
        else -> call.respond(HttpStatusCode.InternalServerError, "InternalServerError")
      }
    }
  }
}

fun Throwable.customRootCause(): Throwable =
  if (cause == null) this else cause!!.customRootCause()