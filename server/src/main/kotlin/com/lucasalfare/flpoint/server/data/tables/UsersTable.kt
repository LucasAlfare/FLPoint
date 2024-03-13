package com.lucasalfare.flpoint.server.data.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object UsersTable : LongIdTable("Users") {

  val login = text("login").uniqueIndex()
  val hashedPassword = text("hashed_password")

  // TODO: other metadata
}