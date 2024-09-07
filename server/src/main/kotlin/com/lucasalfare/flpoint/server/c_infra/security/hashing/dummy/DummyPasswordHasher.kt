package com.lucasalfare.flpoint.server.c_infra.security.hashing.dummy

import com.lucasalfare.flpoint.server.a_domain.PasswordHasher

object DummyPasswordHasher : PasswordHasher {
  private const val DUMMY_SUFIX = "-1234_5678"

  override fun hashed(plain: String) = "$plain$DUMMY_SUFIX"

  override fun plainMatchesHashed(plain: String, hashed: String) =
    hashed.replace(DUMMY_SUFIX, "") == plain
}