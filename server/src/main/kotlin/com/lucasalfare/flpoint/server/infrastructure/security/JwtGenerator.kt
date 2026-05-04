package com.lucasalfare.flpoint.server.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.time.toJavaInstant

data class AppJwtClaims(
  val userId: Int,
  val isAdmin: Boolean,
  val expiresAt: Instant = (Clock.System.now() + Constants.DEFAULT_JWT_EXPIRATION_TIME.minutes)
) {
  companion object {
    const val USER_ID_KEY = "id"
    const val IS_ADMIN_KEY = "is_admin"
  }
}

object JwtGenerator {

  private lateinit var algorithm: Algorithm
  lateinit var verifier: JWTVerifier
    private set

  fun initialize(secret: String) {
    algorithm = Algorithm.HMAC256(secret)
    verifier = JWT.require(algorithm).build()
  }

  fun generate(claims: AppJwtClaims): String {
    return JWT.create()
      .withClaim(AppJwtClaims.USER_ID_KEY, claims.userId)
      .withClaim(AppJwtClaims.IS_ADMIN_KEY, claims.isAdmin)
      .withExpiresAt(claims.expiresAt.toJavaInstant())
      .sign(algorithm)
  }
}
