@file:Suppress("MayBeConstant")

package com.lucasalfare.flpoint.server.c_infra.security.jwt.ktor

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.lucasalfare.flpoint.server.a_domain.model.UserRole

object KtorJwtGenerator {

  private val jwtAlgorithmSignSecret = "JWT_ALGORITHM_SECRET"

  val verifier: JWTVerifier = JWT
    .require(Algorithm.HMAC256(jwtAlgorithmSignSecret))
    .build()

  fun generate(login: String, role: UserRole): String =
    JWT.create()
      .withClaim("login", login)
      .withClaim("role", role.name)
      .sign(Algorithm.HMAC256(jwtAlgorithmSignSecret))
}