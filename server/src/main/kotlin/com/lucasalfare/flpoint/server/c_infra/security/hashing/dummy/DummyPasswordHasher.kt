package com.lucasalfare.flpoint.server.c_infra.security.hashing.dummy

import com.lucasalfare.flpoint.server.a_domain.PasswordHasher

/**
 * A dummy implementation of the [PasswordHasher] interface for testing purposes.
 *
 * The `DummyPasswordHasher` object provides basic password hashing and comparison methods.
 * It appends a fixed suffix to the plain password when hashing and checks for this suffix
 * when comparing hashed passwords.
 */
object DummyPasswordHasher : PasswordHasher {
  private const val DUMMY_SUFFIX = "-1234_5678"

  /**
   * Hashes a plain password by appending a fixed suffix.
   *
   * This method simulates password hashing by appending a predefined suffix to the input
   * plain password.
   *
   * @param plain The plain password to hash.
   * @return The hashed password with the suffix appended.
   */
  override fun hashed(plain: String): String = "$plain$DUMMY_SUFFIX"

  /**
   * Compares a plain password with a hashed password to check if they match.
   *
   * This method checks if the plain password matches the hashed password by removing
   * the predefined suffix from the hashed password and comparing it with the plain password.
   *
   * @param plain The plain password to compare.
   * @param hashed The hashed password to compare against.
   * @return `true` if the plain password matches the hashed password, `false` otherwise.
   */
  override fun plainMatchesHashed(plain: String, hashed: String): Boolean =
    hashed.replace(DUMMY_SUFFIX, "") == plain
}