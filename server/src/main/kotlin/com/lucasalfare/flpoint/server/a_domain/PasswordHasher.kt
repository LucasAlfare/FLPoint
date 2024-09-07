package com.lucasalfare.flpoint.server.a_domain

interface PasswordHasher {

  fun hashed(plain: String): String

  fun plainMatchesHashed(plain: String, hashed: String): Boolean
}