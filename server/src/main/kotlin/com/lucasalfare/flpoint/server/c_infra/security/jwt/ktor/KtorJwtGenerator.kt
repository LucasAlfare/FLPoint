@file:Suppress("MayBeConstant")

package com.lucasalfare.flpoint.server.c_infra.security.jwt.ktor

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.lucasalfare.flpoint.server.a_domain.JwtGenerator

// this abstraction is bad
object KtorJwtGenerator : JwtGenerator {

  private val jwtAlgorithmSignSecret = "JWT_ALGORITHM_SECRET"

  override fun getJwtVerifier(): Any? {
    return JWT
      .require(Algorithm.HMAC256(jwtAlgorithmSignSecret))
      .build()
  }

  override fun generate(
    withClaim: String // login/email
  ): String =
    JWT.create()
//      .withExpiresAt(Date(System.currentTimeMillis() + (60000 * 10)))
      .withClaim("login/email", withClaim)
      .sign(Algorithm.HMAC256(jwtAlgorithmSignSecret))
}