package com.lucasalfare.flpoint.server.data.service

import com.lucasalfare.flpoint.server.data.MyDatabase
import com.lucasalfare.flpoint.server.data.models.ServerResult
import com.lucasalfare.flpoint.server.data.tables.UsersTable
import com.lucasalfare.flpoint.server.security.PasswordHashing
import io.ktor.http.*
import org.jetbrains.exposed.sql.insertAndGetId

object Users {

  suspend fun createUser(login: String, originalPassword: String): ServerResult {
    val id = MyDatabase.dbQuery {
      UsersTable.insertAndGetId {
        it[UsersTable.login] = login
        it[UsersTable.hashedPassword] = PasswordHashing.hashedPassword(originalPassword)
      }.value
    }

    return ServerResult(code = HttpStatusCode.OK, data = id)
  }
}