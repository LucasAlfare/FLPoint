package com.lucasalfare.flpoint.server.a_domain

interface JwtGenerator {

  fun getJwtVerifier(): Any?
  fun generate(withClaim: String): String
}