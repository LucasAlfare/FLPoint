package com.lucasalfare.flpoint.server.security

import org.mindrot.jbcrypt.BCrypt

fun hashPassword(original: String): String? {
  return BCrypt.hashpw(original, BCrypt.gensalt())
}

fun checkPassword(plainPassword: String, original: String) =
  BCrypt.checkpw(plainPassword, original)