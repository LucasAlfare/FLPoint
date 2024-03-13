package com.lucasalfare.flpoint.server.security

import org.mindrot.jbcrypt.BCrypt

object PasswordHashing {
  fun hashedPassword(original: String): String {
    return BCrypt.hashpw(original, BCrypt.gensalt())
  }

  fun checkPassword(plainPassword: String, original: String) =
    BCrypt.checkpw(plainPassword, original)
}