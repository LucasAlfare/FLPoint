package com.lucasalfare.flpoint.server.c_infra.data.memory

import com.lucasalfare.flpoint.server.a_domain.UsersHandler
import com.lucasalfare.flpoint.server.a_domain.model.DatabaseError
import com.lucasalfare.flpoint.server.a_domain.model.User
import com.lucasalfare.flpoint.server.a_domain.model.UserRole

object MemoryUsersHandler : UsersHandler {
  private val users = mutableListOf<User>()

  override suspend fun create(name: String, email: String, hashedPassword: String, role: UserRole): Result<Int> {
    val id = users.size + 1

    if (users.any { it.id == id || it.email == email }) throw DatabaseError()

    users += User(
      id = id,
      name = name,
      email = email,
      hashedPassword = hashedPassword,
      role = role
    )

    return Result.success(id)
  }

  override suspend fun get(id: Int): Result<User> {
    TODO("Not yet implemented")
  }

  override suspend fun get(email: String): Result<User> {
    val search = users.singleOrNull { it.email == email }
    if (search == null) throw DatabaseError()
    return Result.success(search)
  }

  override suspend fun getAll(): Result<List<User>> {
    TODO("Not yet implemented")
  }

  override suspend fun update(
    id: Int,
    name: String?,
    email: String?,
    hashedPassword: String?,
    role: UserRole?
  ): Result<Boolean> {
    TODO("Not yet implemented")
  }

  override suspend fun remove(id: Int): Result<Boolean> {
    TODO("Not yet implemented")
  }

  override suspend fun clear(): Result<Boolean> {
    users.clear()
    return Result.success(true)
  }
}