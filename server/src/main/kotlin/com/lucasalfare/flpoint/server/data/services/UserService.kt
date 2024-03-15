package com.lucasalfare.flpoint.server.data.services

import com.lucasalfare.flpoint.server.data.MyDatabase
import com.lucasalfare.flpoint.server.data.models.ServerResult
import com.lucasalfare.flpoint.server.data.models.User
import com.lucasalfare.flpoint.server.data.tables.UsersTable
import com.lucasalfare.flpoint.server.security.PasswordHashing
import io.ktor.http.*
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

object Users {

  // TODO: does this needs dedicated validations?
  suspend fun createUser(login: String, originalPassword: String): ServerResult {
    val id = MyDatabase.dbQuery {
      UsersTable.insertAndGetId {
        it[UsersTable.login] = login
        it[hashedPassword] = PasswordHashing.hashedPassword(originalPassword)
      }.value
    }

    return ServerResult(code = HttpStatusCode.OK, data = ("id" to id))
  }

  // TODO: refactor this to a dedicated validator
  suspend fun validLogin(login: String, originalPassword: String): ServerResult {
    val search = MyDatabase.dbQuery {
      UsersTable
        .selectAll()
        .where { UsersTable.login eq login }
        .singleOrNull()
    }

    if (search == null) {
      return ServerResult(HttpStatusCode.NotFound, "Login not found. Have you created access before?")
    } else {
      val actualHashed = search[UsersTable.hashedPassword]
      val passwordCheck = PasswordHashing.checkPassword(originalPassword, actualHashed)
      return if (passwordCheck) {
        ServerResult(HttpStatusCode.OK, search[UsersTable.id].value)
      } else {
        ServerResult(HttpStatusCode.NotAcceptable, "Wrong login or password.")
      }
    }
  }

  suspend fun getUserById(id: Long): ServerResult {
    MyDatabase.dbQuery {
      UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()?.let {
        User(
          id = it[UsersTable.id].value,
          login = it[UsersTable.login],
          hashedPassword = it[UsersTable.hashedPassword]
        )
      }
    }?.let {
      return ServerResult(HttpStatusCode.OK, it)
    }

    return ServerResult(HttpStatusCode.NotFound, "User not found")
  }
}