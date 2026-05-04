package com.lucasalfare.flpoint.server.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.lucasalfare.flpoint.server.shared.AuthenticationError
import io.ktor.server.auth.*
import io.ktor.server.request.*
import kotlin.time.toKotlinInstant

fun ApplicationCall.getAppJwtClaims(): AppJwtClaims? {
  val principal = principal<JWTPrincipal>() ?: return null
  val userId = principal.payload.getClaim(AppJwtClaims.USER_ID_KEY) ?: return null
  val isAdmin = principal.payload.getClaim(AppJwtClaims.IS_ADMIN_KEY) ?: return null
  val expiresAt = principal.payload.expiresAt
  return AppJwtClaims(userId.asInt(), isAdmin.asBoolean(), expiresAt.toInstant().toKotlinInstant())
}

fun ApplicationCall.getBearerToken(): String? {
  return request.headers["Authorization"]
    ?.removePrefix("Bearer ")
    ?.trim()
}

fun extractExpirationFromJwt(token: String): Instant {
  val decoded = JWT.decode(token)
  val exp = decoded.expiresAt ?: throw AuthenticationError("Token without expiration")
  return exp.toInstant().toKotlinInstant()
}
