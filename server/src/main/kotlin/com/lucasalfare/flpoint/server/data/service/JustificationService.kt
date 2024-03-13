package com.lucasalfare.flpoint.server.data.service

import com.lucasalfare.flpoint.server.data.models.Justification
import com.lucasalfare.flpoint.server.data.tables.JustificationsTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object Justifications {

  // Função para criar uma nova justificativa
  fun createJustification(date: Long, reason: String, description: String, relatedUserId: Long): Long {
    return transaction {
      JustificationsTable.insertAndGetId {
        it[JustificationsTable.date] = date
        it[JustificationsTable.reason] = reason
        it[JustificationsTable.description] = description
        it[JustificationsTable.relatedUserId] = relatedUserId
      }.value
    }
  }

  // Função para recuperar uma justificativa por ID
  fun getJustificationById(justificationId: Long): Justification? {
    return transaction {
      JustificationsTable.select { JustificationsTable.id eq justificationId }
        .map {
          Justification(
            it[JustificationsTable.id].value,
            it[JustificationsTable.date],
            it[JustificationsTable.reason],
            it[JustificationsTable.description],
            it[JustificationsTable.relatedUserId]
          )
        }
        .singleOrNull()
    }
  }

  // Função para atualizar uma justificativa
  fun updateJustification(justificationId: Long, date: Long, reason: String, description: String, relatedUserId: Long) {
    transaction {
      JustificationsTable.update({ JustificationsTable.id eq justificationId }) {
        it[JustificationsTable.date] = date
        it[JustificationsTable.reason] = reason
        it[JustificationsTable.description] = description
        it[JustificationsTable.relatedUserId] = relatedUserId
      }
    }
  }

  // Função para excluir uma justificativa
  fun deleteJustification(justificationId: Long) {
    transaction {
      JustificationsTable.deleteWhere { JustificationsTable.id eq justificationId }
    }
  }
}