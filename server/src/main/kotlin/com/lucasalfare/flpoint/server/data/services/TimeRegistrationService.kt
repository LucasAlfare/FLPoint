package com.lucasalfare.flpoint.server.data.services

import com.lucasalfare.flpoint.server.data.tables.TimeRegistrationsTable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object TimeRegistrations {

  // Função para criar um novo registro de tempo
  fun createTimeRegistration(date: Long, relatedUserId: Long): Long {
    return transaction {
      TimeRegistrationsTable.insertAndGetId {
        it[TimeRegistrationsTable.date] = date
        it[TimeRegistrationsTable.relatedUserId] = relatedUserId
      }.value
    }
  }
}