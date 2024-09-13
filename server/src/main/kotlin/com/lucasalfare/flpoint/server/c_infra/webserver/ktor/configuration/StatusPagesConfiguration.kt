package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration

import com.lucasalfare.flpoint.server.a_domain.customRootCause
import com.lucasalfare.flpoint.server.a_domain.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

/**
 * Configures status pages for the application to handle exceptions.
 *
 * This function installs the StatusPages feature to manage different types of exceptions
 * and return appropriate HTTP responses. It handles the following exceptions:
 * - `ValidationError`: Responds with HTTP 406 Not Acceptable.
 * - `DatabaseError`: Responds with HTTP 422 Unprocessable Entity.
 * - `LoginError`: Responds with HTTP 401 Unauthorized.
 * - `NoPrivilegeError`: Responds with HTTP 401 Unauthorized.
 * - `UsecaseRuleError`: Responds with HTTP 422 Unprocessable Entity.
 * For any other exceptions, it logs the stack trace and responds with HTTP 500 Internal Server Error.
 */
fun Application.statusPagesConfiguration() {
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      return@exception when (val root = cause.customRootCause()) {
        is ValidationError -> call.respond(HttpStatusCode.NotAcceptable, root.message ?: "ValidationError")
        is DatabaseError -> call.respond(HttpStatusCode.UnprocessableEntity, root.message ?: "DatabaseError")
        is LoginError -> call.respond(HttpStatusCode.Unauthorized, root.message ?: "LoginError")
        is NoPrivilegeError -> call.respond(HttpStatusCode.Unauthorized, root.message ?: "NoPrivilegeError")
        is UsecaseRuleError -> call.respond(HttpStatusCode.UnprocessableEntity, root.message ?: "UsecaseRuleError")
        else -> {
          cause.printStackTrace()
          call.respond(HttpStatusCode.InternalServerError, "InternalServerError")
        }
      }
    }
  }
}