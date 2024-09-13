package com.lucasalfare.flpoint.server.c_infra.data.exposed

import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Users : IntIdTable("Users") {
  val name = text("name")
  val email = text("email").uniqueIndex()
  val hashedPassword = text("hashed_password")
  val role = enumeration<UserRole>("role")
}

object Points : IntIdTable("Points") {
  val relatedUser = integer("related_user").references(Users.id)
  val timestamp = datetime("timestamp")
}