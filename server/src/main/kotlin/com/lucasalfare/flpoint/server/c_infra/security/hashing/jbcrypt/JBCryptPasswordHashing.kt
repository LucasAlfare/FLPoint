package com.lucasalfare.flpoint.server.c_infra.security.hashing.jbcrypt

import com.lucasalfare.flpoint.server.a_domain.PasswordHashing
import org.mindrot.jbcrypt.BCrypt

object JBCryptPasswordHashing : PasswordHashing {
  override fun hashed(plain: String): String = BCrypt.hashpw(plain, BCrypt.gensalt())

  override fun plainMatchHashed(plain: String, hashed: String): Boolean = BCrypt.checkpw(plain, hashed)
}