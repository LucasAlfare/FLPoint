package com.lucasalfare.flpoint.server.c_infra.data.exposed

import com.lucasalfare.flpoint.server.a_domain.UsersHandler
import com.lucasalfare.flpoint.server.a_domain.model.DatabaseError
import com.lucasalfare.flpoint.server.a_domain.model.User
import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

object ExposedUsersHandler : UsersHandler {
  override suspend fun create(
    name: String,
    email: String,
    hashedPassword: String,
    role: UserRole
  ) = try {
    AppDB.exposedQuery {
      Users.insertAndGetId {
        it[Users.name] = name
        it[Users.email] = email
        it[Users.hashedPassword] = hashedPassword
        it[Users.role] = role
      }
    }.let {
      Result.success(it.value)
    }
  } catch (e: Exception) {
    throw DatabaseError()
  }

  override suspend fun get(id: Int): Result<User> {
    TODO("Not yet implemented")
  }

  override suspend fun get(email: String) = try {
    AppDB.exposedQuery {
      Users
        .selectAll()
        .where { Users.email eq email }
        .single()
        .let {
          val search = User(
            id = it[Users.id].value,
            name = it[Users.name],
            email = it[Users.email],
            hashedPassword = it[Users.hashedPassword],
            role = it[Users.role]
          )
          Result.success(search)
        }
    }
  } catch (e: Exception) {
//    throw DatabaseError()
    Result.failure(DatabaseError())
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

  override suspend fun removeAll(): Result<Boolean> {
    TODO("Not yet implemented")
  }
}