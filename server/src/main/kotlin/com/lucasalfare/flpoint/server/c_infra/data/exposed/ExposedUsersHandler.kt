package com.lucasalfare.flpoint.server.c_infra.data.exposed

import com.lucasalfare.flpoint.server.a_domain.UsersHandler
import com.lucasalfare.flpoint.server.a_domain.model.DatabaseError
import com.lucasalfare.flpoint.server.a_domain.model.User
import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object ExposedUsersHandler : UsersHandler {

  // Cria um novo usuário e retorna o ID gerado
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
        throw DatabaseError()
      }
    }

  // Retorna um usuário por ID
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
//      user?.let { Result.success(it) } ?: Result.failure(DatabaseException("User not found"))
      user?.let { Result.success(it) } ?: Result.failure(DatabaseError())
    } catch (e: Exception) {
      throw DatabaseError()
    }
  }

  // Retorna um usuário por email
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
//      user?.let { Result.success(it) } ?: Result.failure(DatabaseException("User not found"))
      user?.let { Result.success(it) } ?: Result.failure(DatabaseError())
    } catch (e: Exception) {
      throw DatabaseError()
    }
  }

  // Retorna todos os usuários
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
      throw DatabaseError()
    }
  }

  // Atualiza um usuário
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
      throw DatabaseError()
    }
  }

  // Remove um usuário por ID
  override suspend fun remove(id: Int): Result<Boolean> = AppDB.exposedQuery {
    try {
      val deletedRows = Users.deleteWhere { Users.id eq id }
      Result.success(deletedRows > 0)
    } catch (e: Exception) {
      throw DatabaseError()
    }
  }

  // Limpa todos os usuários
  override suspend fun clear(): Result<Boolean> = AppDB.exposedQuery {
    try {
      Users.deleteAll()
      Result.success(true)
    } catch (e: Exception) {
      throw DatabaseError()
    }
  }
}