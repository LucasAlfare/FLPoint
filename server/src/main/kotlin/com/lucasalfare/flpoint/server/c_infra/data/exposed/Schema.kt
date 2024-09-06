package com.lucasalfare.flpoint.server.c_infra.data.exposed

import com.lucasalfare.flpoint.server.a_domain.UserType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Users : IntIdTable("Users") {
  val name = text("name")
  val email = text("email")
  val hashedPassword = text("hashed_passord")
  val userType = enumeration<UserType>("user_type")
}

object TimeEntries : IntIdTable("TimeEntries") {
  val relatedUser = integer("related_user").references(Users.id)
  val timestamp = datetime("timestamp")
}