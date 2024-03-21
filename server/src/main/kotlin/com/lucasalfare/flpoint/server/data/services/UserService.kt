package com.lucasalfare.flpoint.server.data.services

import com.lucasalfare.flpoint.server.data.AppDB
import com.lucasalfare.flpoint.server.data.tables.UsersTable
import com.lucasalfare.flpoint.server.data.tables.UsersTable.hashedPassword
import com.lucasalfare.flpoint.server.data.tables.UsersTable.login
import com.lucasalfare.flpoint.server.models.User
import com.lucasalfare.flpoint.server.models.dto.Credentials
import com.lucasalfare.flpoint.server.models.errors.AppResult
import com.lucasalfare.flpoint.server.models.errors.DatabaseError
import com.lucasalfare.flpoint.server.security.PasswordHashing
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

object Users {

  suspend fun createUser(credentials: Credentials): AppResult<Long, DatabaseError> {
    // login is unique. If a repeated login is tried to be inserted, insertion fails
    // this avoids double database lookup to check login existence before inserting
    val id = try {
      AppDB.query {
        UsersTable.insertAndGetId {
          it[login] = credentials.login
          it[hashedPassword] = PasswordHashing.hashedPassword(credentials.password)
        }.value
      }
    } catch (e: Exception) {
      return AppResult.Failure(DatabaseError.Internal)
    }

    return AppResult.Success(id)
  }

  suspend fun getUserById(id: Long): AppResult<User, DatabaseError> {
    AppDB.query {
      UsersTable.selectAll().where { UsersTable.id eq id }
        .singleOrNull()
        ?.let { User(it[UsersTable.id].value, it[login], it[hashedPassword]) }
    }?.let { return AppResult.Success(it) }

    return AppResult.Failure(DatabaseError.NotFound)
  }
}