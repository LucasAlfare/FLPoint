package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration

import com.lucasalfare.flpoint.server.a_domain.model.NoPrivilegeError
import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.util.pipeline.*

/**
 * Extension function for handling authenticated admin users within Ktor route methods.
 * This function checks if the JWT token present in the request has an "Admin" role claim and
 * authorizes the request based on this role. If the user has the "Admin" role, the provided
 * [onSucceedAdminVerification] lambda is executed.
 *
 * @param onSucceedAdminVerification Lambda function to be executed if the user is an admin.
 * @throws NoPrivilegeError if the user does not have the "Admin" role.
 */
suspend fun PipelineContext<Unit, ApplicationCall>.handleAsAuthenticatedAdmin(
  onSucceedAdminVerification: suspend () -> Unit = {}
) {
  // Retrieve the JWT principal from the request
  val principal = call.principal<JWTPrincipal>()

  // Extract the role claim from the JWT payload and determine user role
  val role = enumValueOf<UserRole>(
    principal?.payload?.getClaim("role")?.asString()?.replace("\"", "") ?: UserRole.Standard.name
  )

  // Check if the user has the "Admin" role
  if (role == UserRole.Admin) {
    // Execute the provided lambda if the user is an admin
    onSucceedAdminVerification()
  } else {
    // Throw an exception if the user does not have the required role
    throw NoPrivilegeError()
  }
}