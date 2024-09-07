package com.lucasalfare.flpoint.server.c_infra.webserver.ktor

import com.lucasalfare.flpoint.server.a_domain.model.DatabaseError
import com.lucasalfare.flpoint.server.a_domain.model.LoginError
import com.lucasalfare.flpoint.server.a_domain.model.ValidationError
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import com.lucasalfare.flpoint.server.c_infra.security.jwt.ktor.KtorJwtGenerator
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.loginPostRoute
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.protectedForAdminRoute
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.protectedRoute
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.registerPostRoute
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureAuthentication() {
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

fun Application.configureRouting(userUsecases: UserUsecases) {
  routing {
    protectedRoute()
    protectedForAdminRoute()
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
      when (val root = cause.customRootCause()) {
        is ValidationError -> call.respond(HttpStatusCode.NotAcceptable, root.message ?: "ValidationError")
        is DatabaseError -> call.respond(HttpStatusCode.UnprocessableEntity, root.message ?: "DatabaseError")
        is LoginError -> call.respond(HttpStatusCode.Unauthorized, root.message ?: "LoginError")
        else -> {
          cause.printStackTrace()
          call.respond(HttpStatusCode.InternalServerError, "InternalServerError")
        }
      }
    }
  }
}

fun Throwable.customRootCause(): Throwable =
  if (cause == null) this else cause!!.customRootCause()