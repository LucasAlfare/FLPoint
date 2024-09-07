package com.lucasalfare.flpoint.server.c_infra.security.hashing.jbcrypt

import com.lucasalfare.flpoint.server.a_domain.PasswordHasher
import org.mindrot.jbcrypt.BCrypt

object JBCryptPasswordHasher : PasswordHasher {
  override fun hashed(plain: String): String =
    BCrypt.hashpw(plain, BCrypt.gensalt())

  override fun plainMatchesHashed(plain: String, hashed: String): Boolean =
    BCrypt.checkpw(plain, hashed)
}