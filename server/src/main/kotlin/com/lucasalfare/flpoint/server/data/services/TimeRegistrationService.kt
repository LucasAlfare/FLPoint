package com.lucasalfare.flpoint.server.data.services

import com.lucasalfare.flpoint.server.data.MyDatabase
import com.lucasalfare.flpoint.server.data.models.ServerResult
import com.lucasalfare.flpoint.server.data.tables.TimeRegistrationsTable
import io.ktor.http.*
import org.jetbrains.exposed.sql.insert

// TODO: validate the received time before insert
// TODO: basic validation can be take the last validation and see if their difference is at least higher than a DEFAULT_MIN_REGISTRATION_INTERVAL

object TimeRegistrations {

  suspend fun createTimeRegistration(dateTime: Long, relatedUserId: Long): ServerResult {
    runCatching {
      MyDatabase.dbQuery {
        TimeRegistrationsTable.insert {
          it[TimeRegistrationsTable.dateTime] = dateTime
          it[TimeRegistrationsTable.relatedUserId] = relatedUserId
        }
      }
    }.onFailure {
      return ServerResult(HttpStatusCode.NotAcceptable, "Error creating registration time.")
    }

    return ServerResult(HttpStatusCode.OK, "The registration was created.")
  }
}