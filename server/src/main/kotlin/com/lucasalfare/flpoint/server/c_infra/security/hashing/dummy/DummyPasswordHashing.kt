package com.lucasalfare.flpoint.server.c_infra.security.hashing.dummy

import com.lucasalfare.flpoint.server.a_domain.PasswordHashing

object DummyPasswordHashing : PasswordHashing {
  private const val WEIRD_SUFIX = "-1234_5678"

  override fun hashed(plain: String) = "$plain$WEIRD_SUFIX"

  override fun plainMatchHashed(plain: String, hashed: String) =
    hashed.replace(WEIRD_SUFIX, "") == plain
}