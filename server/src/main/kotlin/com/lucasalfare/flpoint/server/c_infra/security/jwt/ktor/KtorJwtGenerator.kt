@file:Suppress("MayBeConstant")

package com.lucasalfare.flpoint.server.c_infra.security.jwt.ktor

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.lucasalfare.flpoint.server.a_domain.EnvsLoader.loadEnv
import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlin.time.Duration.Companion.minutes

/**
 * Utility object for generating and verifying JSON Web Tokens (JWTs) using HMAC256 algorithm.
 *
 * The `KtorJwtGenerator` object provides methods to generate JWTs with claims and to verify JWTs using
 * a predefined secret and HMAC256 algorithm.
 */
object KtorJwtGenerator {

//  private val jwtAlgorithmSignSecret = "JWT_ALGORITHM_SECRET"
  private val jwtAlgorithmSignSecret = loadEnv("JWT_ALGORITHM_SIGN_SECRET")

  /**
   * The [JWTVerifier] instance used for verifying JWTs.
   * It is configured to use HMAC256 algorithm with the signing secret.
   */
  val verifier: JWTVerifier = JWT
    .require(Algorithm.HMAC256(jwtAlgorithmSignSecret))
    .build()

  /**
   * Generates a JWT with the specified claims.
   *
   * This method creates a JWT containing the user ID, login, and role as claims. It signs the token using
   * the HMAC256 algorithm with a predefined secret.
   *
   * @param id The user ID to include in the JWT.
   * @param login The user login to include in the JWT.
   * @param role The user role to include in the JWT.
   * @return The generated JWT as a string.
   * @throws JWTCreationException if there is an error during JWT creation.
   */
  fun generate(
    id: Int,
    login: String,
    role: UserRole
  ): String {
    return try {
      JWT.create()
        .withClaim("id", id)
        .withClaim("login", login)
        .withClaim("role", role.name)
//        .withExpiresAt((Clock.System.now() + 5.minutes).toJavaInstant())
        .sign(Algorithm.HMAC256(jwtAlgorithmSignSecret))
    } catch (e: JWTCreationException) {
      // Log the exception or handle it as needed
      throw JWTCreationException("Error occurred while creating JWT.", e)
    }
  }
}