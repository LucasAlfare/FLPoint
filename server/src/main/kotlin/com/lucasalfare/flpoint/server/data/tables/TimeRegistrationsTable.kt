package com.lucasalfare.flpoint.server.data.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object TimeRegistrationsTable : LongIdTable("TimeRegistrations") {

  val dateTime = long("date")
  val relatedUserId = long("related_user_id").references(UsersTable.id)

  // TODO: other metadata of registration, such as location, description, etc
}