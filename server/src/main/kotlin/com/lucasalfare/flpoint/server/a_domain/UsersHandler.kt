package com.lucasalfare.flpoint.server.a_domain

import com.lucasalfare.flpoint.server.a_domain.model.User
import com.lucasalfare.flpoint.server.a_domain.model.UserRole

/**
 * Interface that defines a handler for managing users in the system.
 *
 * The `UsersHandler` provides methods to create, retrieve, update, and delete users, as well as to manage user-related data.
 */
interface UsersHandler {

  /**
   * Creates a new user with the given details.
   *
   * @param name The name of the user to be created.
   * @param email The email address of the user to be created.
   * @param hashedPassword The hashed password of the user.
   * @param role The role to be assigned to the user (e.g., Standard or Admin).
   * @return A [Result] containing the ID of the newly created user if successful, or an error otherwise.
   */
  suspend fun create(
    name: String,
    email: String,
    hashedPassword: String,
    role: UserRole
  ): Result<Int>

  /**
   * Retrieves a user by their ID.
   *
   * @param id The unique identifier of the user to retrieve.
   * @return A [Result] containing the [User] if found, or an error otherwise.
   */
  suspend fun get(id: Int): Result<User>

  /**
   * Retrieves a user by their email.
   *
   * @param email The email address of the user to retrieve.
   * @return A [Result] containing the [User] if found, or an error otherwise.
   */
  suspend fun get(email: String): Result<User>

  /**
   * Retrieves all users in the system.
   *
   * @return A [Result] containing a list of all [User] objects, or an error if retrieval fails.
   */
  suspend fun getAll(): Result<List<User>>

  /**
   * Updates an existing user with the provided values.
   *
   * @param id The unique identifier of the user to update.
   * @param name (Optional) The new name for the user.
   * @param email (Optional) The new email address for the user.
   * @param hashedPassword (Optional) The new hashed password for the user.
   * @param role (Optional) The new role to assign to the user.
   * @return A [Result] indicating whether the update was successful.
   */
  suspend fun update(
    id: Int,
    name: String? = null,
    email: String? = null,
    hashedPassword: String? = null,
    role: UserRole? = null
  ): Result<Boolean>

  /**
   * Removes a user by their ID.
   *
   * @param id The unique identifier of the user to be removed.
   * @return A [Result] indicating whether the removal was successful.
   */
  suspend fun remove(id: Int): Result<Boolean>

  /**
   * Clears all users from the system.
   *
   * @return A [Result] indicating whether the operation was successful.
   */
  suspend fun clear(): Result<Boolean>
}