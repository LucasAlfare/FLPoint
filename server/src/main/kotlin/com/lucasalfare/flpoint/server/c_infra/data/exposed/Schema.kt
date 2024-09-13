package com.lucasalfare.flpoint.server.c_infra.data.exposed

import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Database table definition for storing user records.
 *
 * The `Users` table contains columns for storing user information including their name, email, hashed password,
 * and role. The `email` column is unique for each user.
 */
object Users : IntIdTable("Users") {
  /**
   * Column for storing the user's name.
   */
  val name = text("name")

  /**
   * Column for storing the user's email, which must be unique across all users.
   */
  val email = text("email").uniqueIndex()

  /**
   * Column for storing the user's hashed password.
   */
  val hashedPassword = text("hashed_password")

  /**
   * Column for storing the user's role.
   */
  val role = enumeration<UserRole>("role")
}

/**
 * Database table definition for storing point records.
 *
 * The `Points` table contains columns for storing the related user ID and timestamp of the point record.
 * The `relatedUser` column references the `Users` table, establishing a foreign key relationship.
 */
object Points : IntIdTable("Points") {
  /**
   * Column for storing the ID of the user associated with the point record.
   * This column references the `id` column in the `Users` table.
   */
  val relatedUser = integer("related_user").references(Users.id)

  /**
   * Column for storing the timestamp of the point record.
   */
  val timestamp = datetime("timestamp")
}