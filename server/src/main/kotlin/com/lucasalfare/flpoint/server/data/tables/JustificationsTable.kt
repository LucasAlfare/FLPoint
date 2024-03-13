package com.lucasalfare.flpoint.server.data.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object JustificationsTable : LongIdTable("Justifications") {

  val date = long("date")
  val reason = text("reason")
  val description = text("description")
  val relatedUserId = long("related_user_id").references(UsersTable.id)

  // TODO: implement optional attachments
}