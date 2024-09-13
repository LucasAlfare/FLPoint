package com.lucasalfare.flpoint.server.a_domain

/**
 * Interface that defines methods for hashing passwords and verifying hashed passwords.
 *
 * The `PasswordHasher` provides functionality for creating hashed versions of passwords
 * and for checking if a plain text password matches a hashed password.
 */
interface PasswordHasher {

  /**
   * Hashes a plain text password.
   *
   * @param plain The plain text password to be hashed.
   * @return A hashed version of the provided plain text password.
   */
  fun hashed(plain: String): String

  /**
   * Verifies if a plain text password matches a given hashed password.
   *
   * @param plain The plain text password to verify.
   * @param hashed The hashed password to compare against.
   * @return `true` if the plain text password matches the hashed password, `false` otherwise.
   */
  fun plainMatchesHashed(plain: String, hashed: String): Boolean
}