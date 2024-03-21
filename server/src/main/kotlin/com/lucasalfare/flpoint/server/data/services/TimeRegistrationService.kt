package com.lucasalfare.flpoint.server.data.services

import com.lucasalfare.flpoint.server.data.AppDB
import com.lucasalfare.flpoint.server.data.services.validators.TimeRegistrationCreationValidator
import com.lucasalfare.flpoint.server.data.tables.TimeRegistrationsTable
import com.lucasalfare.flpoint.server.models.TimeRegistration
import com.lucasalfare.flpoint.server.models.errors.AppError
import com.lucasalfare.flpoint.server.models.errors.AppResult
import com.lucasalfare.flpoint.server.models.errors.DatabaseError
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object TimeRegistrations {

  suspend fun createTimeRegistration(dateTime: Long, relatedUserId: Long): AppResult<Unit, AppError> {
    when (val validationResult = TimeRegistrationCreationValidator(dateTime, relatedUserId).validate()) {
      is AppResult.Success -> {
        try {
          AppDB.query {
            TimeRegistrationsTable.insert {
              it[TimeRegistrationsTable.dateTime] = dateTime
              it[TimeRegistrationsTable.relatedUserId] = relatedUserId
            }
          }
        } catch (e: Exception) {
          return AppResult.Failure(DatabaseError.Internal)
        }
      }

      is AppResult.Failure -> {
        return validationResult
      }
    }

    return AppResult.Success(Unit)
  }

  suspend fun getLastRegistrationByUserId(userId: Long): AppResult<TimeRegistration, DatabaseError> {
    AppDB.query {
      TimeRegistrationsTable
        .selectAll()
        .where { TimeRegistrationsTable.relatedUserId eq userId }
        .orderBy(TimeRegistrationsTable.dateTime) // sorting needed?
        .lastOrNull()
        ?.let { TimeRegistration(dateTime = it[TimeRegistrationsTable.dateTime]) }
    }?.let { return AppResult.Success(it) }

    return AppResult.Failure(DatabaseError.NotFound)
  }
}