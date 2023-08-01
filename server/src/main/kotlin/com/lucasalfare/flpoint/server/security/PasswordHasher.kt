package com.lucasalfare.flpoint.server.security

import com.lucasalfare.flpoint.server.model.Credentials
import org.mindrot.jbcrypt.BCrypt

fun Credentials.hashPassword() {
  password = BCrypt.hashpw(password, BCrypt.gensalt())
}

fun Credentials.checkPassword(plainPassword: String) =
  BCrypt.checkpw(plainPassword, this.password)