package com.lucasalfare.flpoint.server.c_infra.security.hashing.jbcrypt

import com.lucasalfare.flpoint.server.a_domain.PasswordHasher
import org.mindrot.jbcrypt.BCrypt

/**
 * Implementation of the [PasswordHasher] interface using BCrypt for password hashing and verification.
 *
 * The `JBCryptPasswordHasher` object provides methods for hashing passwords using the BCrypt algorithm
 * and for verifying plain passwords against hashed passwords.
 */
object JBCryptPasswordHasher : PasswordHasher {

  /**
   * Hashes a plain password using BCrypt.
   *
   * This method generates a BCrypt hash of the provided plain password. It uses BCrypt's `gensalt` method
   * to generate a salt and then hashes the password with this salt.
   *
   * @param plain The plain password to hash.
   * @return The hashed password as a BCrypt hash.
   * @throws IllegalArgumentException if the plain password is empty or null.
   */
  override fun hashed(plain: String): String {
    if (plain.isEmpty()) throw IllegalArgumentException("Password cannot be empty.")
    return BCrypt.hashpw(plain, BCrypt.gensalt())
  }

  /**
   * Compares a plain password with a hashed password to check if they match.
   *
   * This method verifies that the provided plain password matches the hashed password using BCrypt's
   * `checkpw` method.
   *
   * @param plain The plain password to compare.
   * @param hashed The hashed password to compare against.
   * @return `true` if the plain password matches the hashed password, `false` otherwise.
   * @throws IllegalArgumentException if the hashed password is empty or null.
   */
  override fun plainMatchesHashed(plain: String, hashed: String): Boolean {
    if (hashed.isEmpty()) throw IllegalArgumentException("Hashed password cannot be empty.")
    return BCrypt.checkpw(plain, hashed)
  }
}