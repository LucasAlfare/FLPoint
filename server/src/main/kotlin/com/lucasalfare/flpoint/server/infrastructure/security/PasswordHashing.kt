package com.lucasalfare.flpoint.server.infrastructure.security

import com.lucasalfare.flpoint.server.shared.RuleViolatedError
import org.mindrot.jbcrypt.BCrypt

object PasswordHashing {
  fun hashed(plain: String): String {
    if (plain.isEmpty()) throw RuleViolatedError("Password cannot be empty.")
    return BCrypt.hashpw(plain, BCrypt.gensalt())
  }

  fun plainMatchesHashed(plain: String, hashed: String): Boolean {
    if (hashed.isEmpty()) throw RuleViolatedError("Hashed password cannot be empty.")
    return BCrypt.checkpw(plain, hashed)
  }
}
