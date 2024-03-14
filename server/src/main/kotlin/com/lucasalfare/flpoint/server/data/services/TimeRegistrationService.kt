package com.lucasalfare.flpoint.server.data.services

import com.lucasalfare.flpoint.server.Rules
import com.lucasalfare.flpoint.server.data.MyDatabase
import com.lucasalfare.flpoint.server.data.models.ServerResult
import com.lucasalfare.flpoint.server.data.models.TimeRegistration
import com.lucasalfare.flpoint.server.data.services.validators.TimeRegistrationCreationValidator
import com.lucasalfare.flpoint.server.data.tables.TimeRegistrationsTable
import com.lucasalfare.flpoint.server.toErrorResponseString
import io.ktor.http.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object TimeRegistrations {

  suspend fun createTimeRegistration(dateTime: Long, relatedUserId: Long): ServerResult {
    if (
      TimeRegistrationCreationValidator(
        actualValidationDateTime = dateTime,
        targetUserId = relatedUserId
      ).isValid()
    ) {
      runCatching {
        MyDatabase.dbQuery {
          TimeRegistrationsTable.insert {
            it[TimeRegistrationsTable.dateTime] = dateTime
            it[TimeRegistrationsTable.relatedUserId] = relatedUserId
          }
        }
      }.onFailure {
        // here we can face problems related to database insertion
        // then we return accordingly
        return ServerResult(HttpStatusCode.InternalServerError, it.toErrorResponseString())
      }
      return ServerResult(HttpStatusCode.OK, "The registration was created.")
    } else {
      return ServerResult(
        HttpStatusCode.NotAcceptable,
        "Time registration not created. Must wait at least ${Rules.DEFAULT_MIN_REGISTRATION_INTERVAL} milliseconds before registering again."
      )
    }
  }

  suspend fun getLastRegistrationByUserId(userId: Long): ServerResult {
    val search = MyDatabase.dbQuery {
      TimeRegistrationsTable
        .selectAll()
        .where { TimeRegistrationsTable.relatedUserId eq userId }
        .last()
        .let {
          TimeRegistration(dateTime = it[TimeRegistrationsTable.dateTime])
        }
    }

    return ServerResult(HttpStatusCode.OK, search)
  }
}