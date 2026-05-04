package com.lucasalfare.flpoint.server.config

import com.lucasalfare.flpoint.server.shared.Constants
import com.lucasalfare.flpoint.server.shared.DataHandlingError
import com.lucasalfare.flpoint.server.shared.AuthenticationError
import com.lucasalfare.flpoint.server.shared.ValidationError
import com.lucasalfare.flpoint.server.shared.NoPrivilegeError
import com.lucasalfare.flpoint.server.shared.RuleViolatedError
import com.lucasalfare.flpoint.server.shared.customRootCause
import com.lucasalfare.flpoint.server.infrastructure.security.authenticationConfiguration
import com.lucasalfare.flpoint.server.application.route.routesHandlers
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

/**
 * Status pages plugin help us to keep track of all error fired in the
 * application flow. This is useful to make the entities of the app
 * throw errors instead returning then. This is nice due we have a
 * limited kind of errors, and they can be caught here, in a centered
 * place.
 *
 * TODO: improve kind of errors and messages.
 */
fun Application.statusPagesConfiguration() {
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      Constants.logger.error("Unhandled exception on ${call.request.uri}", cause)
      return@exception when (val root = cause.customRootCause()) {
        is DataHandlingError -> call.respond(HttpStatusCode.InternalServerError, root.message ?: "DataHandlingError")
        is AuthenticationError -> call.respond(HttpStatusCode.Unauthorized, root.message ?: "AuthenticationError")
        is ValidationError -> call.respond(HttpStatusCode.UnprocessableEntity, root.message ?: "ValidationError")
        is NoPrivilegeError -> call.respond(HttpStatusCode.Forbidden, root.message ?: "NoPrivilegeError")
        is RuleViolatedError -> call.respond(
          HttpStatusCode.UnprocessableEntity,
          root.message ?: "RuleViolatedError"
        )

        else -> {
          Constants.logger.trace(cause.message)
          if (cause is BadRequestException) {
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "BadRequestException")
          } else {
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "InternalServerError")
          }
        }
      }
    }
  }
}

fun Application.serializationConfiguration() {
  install(ContentNegotiation) { json(Json { isLenient = false }) }
}

fun Application.configureCORS() {
  install(CORS) {
    allowHeader(HttpHeaders.Authorization)
    allowCredentials = true //may be useful
    allowNonSimpleContentTypes = true //may be useful
    listOf(HttpMethod.Patch, HttpMethod.Get, HttpMethod.Post, HttpMethod.Delete).forEach {
      allowMethod(it)
    }
    anyHost()
  }
}

fun Application.initKtorConfiguration() {
  install(CallLogging) {
    level = Level.INFO
    format { call ->
      val status = call.response.status()
      val httpMethod = call.request.httpMethod.value
      val userAgent = call.request.headers["User-Agent"]
      "Status: $status, HTTP method: $httpMethod, User agent: $userAgent"
    }
  }
  authenticationConfiguration()
  statusPagesConfiguration()
  serializationConfiguration()
  configureCORS()
  routing { routesHandlers() }
}
