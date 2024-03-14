package com.lucasalfare.flpoint.server.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*


internal const val DEFAULT_JWT_CONFIG = "my-jwt-auth"

internal object JwtConfig {

  // Fields must comes from ENV variables
  private const val SECRET = "MySecret"
  private const val ISSUER = "FLPoint"
  private const val EXPIRATION_TIME = (1000 * 60) * 3 // 3 minutes

  private val algorithm = Algorithm.HMAC256(SECRET)

  private val verifier: JWTVerifier = JWT
    .require(algorithm)
    .withIssuer(ISSUER)
    .build()

  fun getVerifier(): JWTVerifier = verifier

  fun generateJwt(userId: Long): String = JWT
    .create()
    .withSubject("Authentication")
    .withIssuer(ISSUER)
    .withClaim("user_id", userId)
    .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
    .sign(algorithm)
}