package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration

import com.lucasalfare.flpoint.server.a_domain.customRootCause
import com.lucasalfare.flpoint.server.a_domain.model.DatabaseError
import com.lucasalfare.flpoint.server.a_domain.model.LoginError
import com.lucasalfare.flpoint.server.a_domain.model.UsecaseRuleError
import com.lucasalfare.flpoint.server.a_domain.model.ValidationError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.statusPagesConfiguration() {
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      when (val root = cause.customRootCause()) {
        is ValidationError -> call.respond(HttpStatusCode.NotAcceptable, root.message ?: "ValidationError")
        is DatabaseError -> call.respond(HttpStatusCode.UnprocessableEntity, root.message ?: "DatabaseError")
        is LoginError -> call.respond(HttpStatusCode.Unauthorized, root.message ?: "LoginError")
        is UsecaseRuleError -> call.respond(HttpStatusCode.UnprocessableEntity, root.message ?: "UsecaseRuleError")
        else -> {
          cause.printStackTrace()
          call.respond(HttpStatusCode.InternalServerError, "InternalServerError")
        }
      }
    }
  }
}