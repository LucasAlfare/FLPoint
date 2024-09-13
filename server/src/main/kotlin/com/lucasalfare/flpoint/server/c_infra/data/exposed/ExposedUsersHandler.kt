package com.lucasalfare.flpoint.server.c_infra.data.exposed

import com.lucasalfare.flpoint.server.a_domain.UsersHandler
import com.lucasalfare.flpoint.server.a_domain.model.DatabaseError
import com.lucasalfare.flpoint.server.a_domain.model.User
import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Implementation of [UsersHandler] using Exposed framework for database operations.
 *
 * The `ExposedUsersHandler` object provides concrete implementations for user management operations
 * including creating, retrieving, updating, removing, and clearing users from the database.
 */
object ExposedUsersHandler : UsersHandler {

  /**
   * Creates a new user and returns the generated user ID.
   *
   * This method inserts a new user record into the database with the specified name, email, hashed password,
   * and role. On success, it returns the ID of the newly created user.
   *
   * @param name The name of the user.
   * @param email The email of the user.
   * @param hashedPassword The hashed password of the user.
   * @param role The role of the user.
   * @return A [Result] containing the ID of the newly created user if successful, or a [DatabaseError] otherwise.
   * @throws DatabaseError If an error occurs during database operations.
   */
  override suspend fun create(name: String, email: String, hashedPassword: String, role: UserRole): Result<Int> =
    AppDB.exposedQuery {
      try {
        val userId = Users.insertAndGetId {
          it[Users.name] = name
          it[Users.email] = email
          it[Users.hashedPassword] = hashedPassword
          it[Users.role] = role
        }.value
        Result.success(userId)
      } catch (e: Exception) {
        Result.failure(DatabaseError()) // Changed to Result.failure
      }
    }

  /**
   * Retrieves a user by ID.
   *
   * This method fetches the user record with the specified ID from the database.
   *
   * @param id The ID of the user to retrieve.
   * @return A [Result] containing the user if found, or a [DatabaseError] if the user is not found or an error occurs.
   * @throws DatabaseError If an error occurs during database operations.
   */
  override suspend fun get(id: Int): Result<User> = AppDB.exposedQuery {
    try {
      val user = Users.selectAll().where { Users.id eq id }.singleOrNull()?.let {
        User(
          id = it[Users.id].value,
          name = it[Users.name],
          email = it[Users.email],
          hashedPassword = it[Users.hashedPassword],
          role = it[Users.role]
        )
      }
      user?.let { Result.success(it) } ?: Result.failure(DatabaseError())
    } catch (e: Exception) {
      Result.failure(DatabaseError()) // Changed to Result.failure
    }
  }

  /**
   * Retrieves a user by email.
   *
   * This method fetches the user record with the specified email from the database.
   *
   * @param email The email of the user to retrieve.
   * @return A [Result] containing the user if found, or a [DatabaseError] if the user is not found or an error occurs.
   * @throws DatabaseError If an error occurs during database operations.
   */
  override suspend fun get(email: String): Result<User> = AppDB.exposedQuery {
    try {
      val user = Users.selectAll().where { Users.email eq email }.singleOrNull()?.let {
        User(
          id = it[Users.id].value,
          name = it[Users.name],
          email = it[Users.email],
          hashedPassword = it[Users.hashedPassword],
          role = it[Users.role]
        )
      }
      user?.let { Result.success(it) } ?: Result.failure(DatabaseError())
    } catch (e: Exception) {
      Result.failure(DatabaseError()) // Changed to Result.failure
    }
  }

  /**
   * Retrieves all users.
   *
   * This method fetches all user records from the database.
   *
   * @return A [Result] containing a list of all users if successful, or a [DatabaseError] if an error occurs.
   * @throws DatabaseError If an error occurs during database operations.
   */
  override suspend fun getAll(): Result<List<User>> = AppDB.exposedQuery {
    try {
      val users = Users.selectAll().map {
        User(
          id = it[Users.id].value,
          name = it[Users.name],
          email = it[Users.email],
          hashedPassword = it[Users.hashedPassword],
          role = it[Users.role]
        )
      }
      Result.success(users)
    } catch (e: Exception) {
      Result.failure(DatabaseError()) // Changed to Result.failure
    }
  }

  /**
   * Updates a user with the specified ID.
   *
   * This method updates the user record with the specified ID, applying any provided changes to name, email,
   * hashed password, and role.
   *
   * @param id The ID of the user to update.
   * @param name Optional new name for the user.
   * @param email Optional new email for the user.
   * @param hashedPassword Optional new hashed password for the user.
   * @param role Optional new role for the user.
   * @return A [Result] containing `true` if the update was successful, or `false` otherwise.
   * @throws DatabaseError If an error occurs during database operations.
   */
  override suspend fun update(
    id: Int,
    name: String?,
    email: String?,
    hashedPassword: String?,
    role: UserRole?
  ): Result<Boolean> = AppDB.exposedQuery {
    try {
      val updatedRows = Users.update({ Users.id eq id }) {
        if (name != null) it[Users.name] = name
        if (email != null) it[Users.email] = email
        if (hashedPassword != null) it[Users.hashedPassword] = hashedPassword
        if (role != null) it[Users.role] = role
      }
      Result.success(updatedRows > 0)
    } catch (e: Exception) {
      Result.failure(DatabaseError()) // Changed to Result.failure
    }
  }

  /**
   * Removes a user by ID.
   *
   * This method deletes the user record with the specified ID from the database.
   *
   * @param id The ID of the user to remove.
   * @return A [Result] containing `true` if the removal was successful, or `false` otherwise.
   * @throws DatabaseError If an error occurs during database operations.
   */
  override suspend fun remove(id: Int): Result<Boolean> = AppDB.exposedQuery {
    try {
      val deletedRows = Users.deleteWhere { Users.id eq id }
      Result.success(deletedRows > 0)
    } catch (e: Exception) {
      Result.failure(DatabaseError()) // Changed to Result.failure
    }
  }

  /**
   * Clears all users from the database.
   *
   * This method deletes all user records from the database.
   *
   * @return A [Result] containing `true` if the operation was successful, or `false` otherwise.
   * @throws DatabaseError If an error occurs during database operations.
   */
  override suspend fun clear(): Result<Boolean> = AppDB.exposedQuery {
    try {
      Users.deleteAll()
      Result.success(true)
    } catch (e: Exception) {
      Result.failure(DatabaseError()) // Changed to Result.failure
    }
  }
}