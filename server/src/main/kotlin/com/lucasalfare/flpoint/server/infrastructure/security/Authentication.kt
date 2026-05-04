package com.lucasalfare.flpoint.server.infrastructure.security

import com.lucasalfare.flpoint.server.shared.NoPrivilegeError
import com.lucasalfare.flpoint.server.shared.AuthenticationError
import com.lucasalfare.flpoint.server.infrastructure.persistence.ExposedDataCRUD
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Application.authenticationConfiguration() {
  install(Authentication) {
    jwt("flpoint-jwt-auth") {
      verifier(JwtGenerator.verifier)

      validate { jwtCredential ->
        val jwtToken = this.request.headers["Authorization"]
          ?.removePrefix("Bearer ")
          ?.trim()

        // case when token is not present
        if (jwtToken.isNullOrBlank()) {
          return@validate null
        }

        // verifies if the token is in blacklist, if so, don't validate
        if (ExposedDataCRUD.jwtBlackListContains(jwtToken)) {
          return@validate null
        }

        val payload = jwtCredential.payload

        val idClaim = payload.getClaim(AppJwtClaims.USER_ID_KEY)
        val isAdminClaim = payload.getClaim(AppJwtClaims.IS_ADMIN_KEY)

        // explicitly checks claims existence
        if (idClaim.isNull || isAdminClaim.isNull) {
          return@validate null
        }

        val userId = idClaim.asInt()

        // also, as extra, checks "invalid" ID
        if (userId <= 0) {
          return@validate null
        }

        JWTPrincipal(payload)
      }
    }
  }
}

suspend fun RoutingContext.handleAsAuthorizedAdmin(
  onSucceedAdminVerification: suspend () -> Unit = {}
) {
  val claims = call.getAppJwtClaims()
    ?: throw AuthenticationError("Invalid JWT claims")

  val isAdmin = ExposedDataCRUD.isUserAdmin(claims.userId)

  if (isAdmin) {
    onSucceedAdminVerification()
  } else {
    throw NoPrivilegeError()
  }
}
