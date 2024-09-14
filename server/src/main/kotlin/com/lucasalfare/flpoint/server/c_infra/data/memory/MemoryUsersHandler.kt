package com.lucasalfare.flpoint.server.c_infra.data.memory

import com.lucasalfare.flpoint.server.a_domain.UsersHandler
import com.lucasalfare.flpoint.server.a_domain.model.DatabaseError
import com.lucasalfare.flpoint.server.a_domain.model.User
import com.lucasalfare.flpoint.server.a_domain.model.UserRole

/**
 * Implementation of [UsersHandler] using an in-memory data structure for user management.
 *
 * The `MemoryUsersHandler` object provides in-memory storage and operations for user management,
 * including creating, retrieving, and clearing users.
 */
object MemoryUsersHandler : UsersHandler {
  private val users = mutableListOf<User>()

  /**
   * Creates a new user and returns the generated user ID.
   *
   * This method adds a new user to the in-memory list with the specified details.
   * It throws a [DatabaseError] if a user with the same ID or email already exists.
   *
   * @param name The name of the user.
   * @param email The email of the user.
   * @param hashedPassword The hashed password of the user.
   * @param role The role assigned to the user.
   * @return A [Result] containing the ID of the newly created user if successful, or a [DatabaseError] otherwise.
   * @throws DatabaseError If a user with the same ID or email already exists.
   */
  override suspend fun create(name: String, email: String, hashedPassword: String, role: UserRole): Result<Int> {
    val id = users.size + 1

    if (users.any { it.id == id || it.email == email }) {
      throw DatabaseError("Error on inserting user info in database.")
    }

    users += User(
      id = id,
      name = name,
      email = email,
      hashedPassword = hashedPassword,
      role = role
    )

    return Result.success(id)
  }

  /**
   * Retrieves a user by ID.
   *
   * This method is not yet implemented.
   *
   * @param id The ID of the user to retrieve.
   * @return A [Result] containing the user if successful, or a [DatabaseError] if the user is not found.
   * @throws NotImplementedError Since this method is not yet implemented.
   */
  override suspend fun get(id: Int): Result<User> {
    TODO("Not yet implemented")
  }

  /**
   * Retrieves a user by email.
   *
   * This method searches for a user with the specified email in the in-memory list.
   * It throws a [DatabaseError] if no user with the given email is found.
   *
   * @param email The email of the user to retrieve.
   * @return A [Result] containing the user if found, or a [DatabaseError] if not found.
   * @throws DatabaseError If no user with the specified email is found.
   */
  override suspend fun get(email: String): Result<User> {
    val search = users.singleOrNull { it.email == email }
    if (search == null) throw DatabaseError()
    return Result.success(search)
  }

  /**
   * Retrieves all users.
   *
   * This method is not yet implemented.
   *
   * @return A [Result] containing a list of all users if successful, or a [DatabaseError] if an error occurs.
   * @throws NotImplementedError Since this method is not yet implemented.
   */
  override suspend fun getAll(): Result<List<User>> {
    TODO("Not yet implemented")
  }

  /**
   * Updates a user.
   *
   * This method is not yet implemented.
   *
   * @param id The ID of the user to update.
   * @param name The new name of the user.
   * @param email The new email of the user.
   * @param hashedPassword The new hashed password of the user.
   * @param role The new role of the user.
   * @return A [Result] containing `true` if the update was successful, or `false` otherwise.
   * @throws NotImplementedError Since this method is not yet implemented.
   */
  override suspend fun update(
    id: Int,
    name: String?,
    email: String?,
    hashedPassword: String?,
    role: UserRole?
  ): Result<Boolean> {
    TODO("Not yet implemented")
  }

  /**
   * Removes a user by ID.
   *
   * This method is not yet implemented.
   *
   * @param id The ID of the user to remove.
   * @return A [Result] containing `true` if the user was successfully removed, or `false` otherwise.
   * @throws NotImplementedError Since this method is not yet implemented.
   */
  override suspend fun remove(id: Int): Result<Boolean> {
    TODO("Not yet implemented")
  }

  /**
   * Clears all users from the in-memory storage.
   *
   * This method removes all user records from the in-memory list.
   *
   * @return A [Result] containing `true` if the operation was successful, or a [DatabaseError] if an error occurs.
   * @throws DatabaseError If an error occurs during the clearing process.
   */
  override suspend fun clear(): Result<Boolean> {
    users.clear()
    return Result.success(true)
  }
}