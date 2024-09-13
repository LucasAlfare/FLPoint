package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration

import com.lucasalfare.flpoint.server.a_domain.model.NoPrivilegeError
import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.util.pipeline.*

// Acts like a plugin to the route methods context.
// Checks if a good JWT contains an "Admin" role claim, then authorizes.
suspend fun PipelineContext<Unit, ApplicationCall>.handleAsAuthenticatedAdmin(
  onSuccessAdminVerification: suspend () -> Unit = {}
) {
  val principal = call.principal<JWTPrincipal>()

  val role = enumValueOf<UserRole>(
    principal?.payload?.getClaim("role")?.asString()?.replace("\"", "") ?: UserRole.Standard.name
  )

  if (role == UserRole.Admin) {
    onSuccessAdminVerification()
  } else {
    throw NoPrivilegeError()
  }
}