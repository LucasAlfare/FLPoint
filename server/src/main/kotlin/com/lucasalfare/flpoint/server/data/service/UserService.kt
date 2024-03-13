package com.lucasalfare.flpoint.server.data.service

import com.lucasalfare.flpoint.server.data.models.User
import com.lucasalfare.flpoint.server.data.tables.UsersTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object Users {
  // Função para criar um novo usuário
  fun createUser(login: String, hashedPassword: String): Long {
    return transaction {
      UsersTable.insertAndGetId {
        it[UsersTable.login] = login
        it[UsersTable.hashedPassword] = hashedPassword
      }.value
    }
  }

  // Função para recuperar um usuário por ID
  fun getUserById(userId: Long): User? {
    return transaction {
      UsersTable.select { UsersTable.id eq userId }
        .map { User(it[UsersTable.id].value, it[UsersTable.login], it[UsersTable.hashedPassword]) }
        .singleOrNull()
    }
  }

  // Função para atualizar um usuário
  fun updateUser(userId: Long, login: String, hashedPassword: String) {
    transaction {
      UsersTable.update({ UsersTable.id eq userId }) {
        it[UsersTable.login] = login
        it[UsersTable.hashedPassword] = hashedPassword
      }
    }
  }

  // Função para excluir um usuário
  fun deleteUser(userId: Long) {
    transaction {
      UsersTable.deleteWhere { UsersTable.id eq userId }
    }
  }
}