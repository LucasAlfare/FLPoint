package com.lucasalfare.flpoint.server.data.services.validators

import com.lucasalfare.flpoint.server.data.AppDB
import com.lucasalfare.flpoint.server.data.tables.UsersTable
import com.lucasalfare.flpoint.server.models.User
import com.lucasalfare.flpoint.server.models.dto.Credentials
import com.lucasalfare.flpoint.server.models.errors.AppResult
import com.lucasalfare.flpoint.server.models.errors.DatabaseError
import com.lucasalfare.flpoint.server.security.PasswordHashing
import org.jetbrains.exposed.sql.selectAll

data class UserExistenceByCredentialsValidator(val credentials: Credentials) : Validator<Long, DatabaseError> {
  override suspend fun validate(): AppResult<Long, DatabaseError> {
    val search = AppDB.query {
      UsersTable
        .selectAll()
        .where { UsersTable.login eq credentials.login }
        .singleOrNull()
    }

    if (search == null)
      return AppResult.Failure(DatabaseError.NotFound)

    search.let {
      User(
        it[UsersTable.id].value,
        it[UsersTable.login],
        it[UsersTable.hashedPassword]
      )
    }.let {
      if (PasswordHashing.checkPassword(credentials.password, it.hashedPassword)) {
        return AppResult.Success(it.id)
      }
    }

    return AppResult.Failure(DatabaseError.NotFound)
  }
}