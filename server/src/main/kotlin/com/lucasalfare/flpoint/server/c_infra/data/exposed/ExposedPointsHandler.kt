package com.lucasalfare.flpoint.server.c_infra.data.exposed

import com.lucasalfare.flpoint.server.a_domain.PointsHandler
import com.lucasalfare.flpoint.server.a_domain.model.DatabaseError
import com.lucasalfare.flpoint.server.a_domain.model.Point
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

object ExposedPointsHandler : PointsHandler {

  // Cria um novo ponto e retorna o ID gerado
  override suspend fun create(relatedUser: Int, timestamp: Instant): Result<Int> = AppDB.exposedQuery {
    try {
      val pointId = Points.insertAndGetId {
        it[Points.relatedUser] = relatedUser
        it[Points.timestamp] = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
      }.value
      Result.success(pointId)
    } catch (e: Exception) {
//      Result.failure(DatabaseException("Failed to create point: ${e.message}", e))
      Result.failure(DatabaseError())
    }
  }

  // Retorna todos os pontos
  override suspend fun get(): Result<List<Point>> = AppDB.exposedQuery {
    try {
      val points = Points.selectAll().map {
        Point(
          id = it[Points.id].value,
          relatedUserId = it[Points.relatedUser],
          timestamp = it[Points.timestamp].toInstant(TimeZone.currentSystemDefault())
        )
      }
      Result.success(points)
    } catch (e: Exception) {
//      Result.failure(DatabaseException("Failed to retrieve points: ${e.message}", e))
      Result.failure(DatabaseError())
    }
  }

  // Retorna os pontos de um usuário específico
  override suspend fun get(relatedUser: Int): Result<List<Point>> = AppDB.exposedQuery {
    try {
      val points = Points.selectAll().where { Points.relatedUser eq relatedUser }.map {
        Point(
          id = it[Points.id].value,
          relatedUserId = it[Points.relatedUser],
          timestamp = it[Points.timestamp].toInstant(TimeZone.UTC)
        )
      }
      Result.success(points)
    } catch (e: Exception) {
//      Result.failure(DatabaseException("Failed to retrieve points for user: ${e.message}", e))
      Result.failure(DatabaseError())
    }
  }

  // Deleta um ponto por ID
  override suspend fun delete(id: Int): Boolean = AppDB.exposedQuery {
    try {
      val deletedRows = Points.deleteWhere { Points.id eq id }
      deletedRows > 0
    } catch (e: Exception) {
      false
    }
  }

  // Limpa todos os pontos
  override suspend fun clear(): Result<Boolean> = AppDB.exposedQuery {
    try {
      Points.deleteAll()
      Result.success(true)
    } catch (e: Exception) {
//      Result.failure(DatabaseException("Failed to clear points: ${e.message}", e))
      Result.failure(DatabaseError())
    }
  }
}
