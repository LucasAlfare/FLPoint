package com.lucasalfare.flpoint.server.infrastructure.persistence

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core varchar
import org.jetbrains.exposed.v1.core text
import org.jetbrains.exposed.v1.core bool
import org.jetbrains.exposed.v1.core integer
import org.jetbrains.exposed.v1.core timestamp
import org.jetbrains.exposed.v1.core references

object Users : IntIdTable("Users") {
  val name = varchar("name", 255)
  val email = varchar("email", 255).uniqueIndex()
  val hashedPassword = varchar("hashed_password", 255)
  val timeZone = text("time_zone")
  val isAdmin = bool("is_admin").default(false)
}

object Points : IntIdTable("Points") {
  val relatedUserId = integer("related_user_id").references(Users.id)
  val instant = timestamp("instant")
}

// TODO: include reference to user that holds this jwt
object JwtBlacklist : IntIdTable("JwtBlackList") {
  val jwt = text("jwt").uniqueIndex()
  val expiresAt = timestamp("expires_at").index()
}
