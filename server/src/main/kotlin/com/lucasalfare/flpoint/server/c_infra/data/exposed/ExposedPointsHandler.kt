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

/**
 * Implementation of [PointsHandler] using Exposed framework for database operations.
 *
 * The `ExposedPointsHandler` object provides concrete implementations for point management operations
 * including creating, retrieving, deleting, and clearing points from the database.
 */
object ExposedPointsHandler : PointsHandler {

  /**
   * Creates a new point and returns the generated point ID.
   *
   * This method inserts a new point record into the database with the specified related user ID and timestamp.
   * On success, it returns the ID of the newly created point.
   *
   * @param relatedUser The ID of the user associated with the point.
   * @param timestamp The timestamp of the point.
   * @return A [Result] containing the ID of the newly created point if successful, or a [DatabaseError] otherwise.
   * @throws DatabaseError If an error occurs during database operations.
   */
  override suspend fun create(relatedUser: Int, timestamp: Instant): Result<Int> = AppDB.exposedQuery {
    try {
      val pointId = Points.insertAndGetId {
        it[Points.relatedUser] = relatedUser
        it[Points.timestamp] = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
      }.value
      Result.success(pointId)
    } catch (e: Exception) {
      Result.failure(DatabaseError()) // Changed to Result.failure
    }
  }

  /**
   * Retrieves all points.
   *
   * This method fetches all point records from the database.
   *
   * @return A [Result] containing a list of all points if successful, or a [DatabaseError] if an error occurs.
   * @throws DatabaseError If an error occurs during database operations.
   */
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
      Result.failure(DatabaseError()) // Changed to Result.failure
    }
  }

  /**
   * Retrieves points associated with a specific user.
   *
   * This method fetches all point records for the user with the specified ID from the database.
   *
   * @param relatedUser The ID of the user whose points are to be retrieved.
   * @return A [Result] containing a list of points for the specified user if successful, or a [DatabaseError] if an error occurs.
   * @throws DatabaseError If an error occurs during database operations.
   */
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
      Result.failure(DatabaseError()) // Changed to Result.failure
    }
  }

  /**
   * Deletes a point by ID.
   *
   * This method removes the point record with the specified ID from the database.
   *
   * @param id The ID of the point to delete.
   * @return `true` if the point was successfully deleted, `false` otherwise.
   * @throws DatabaseError If an error occurs during database operations.
   */
  override suspend fun delete(id: Int): Boolean = AppDB.exposedQuery {
    try {
      val deletedRows = Points.deleteWhere { Points.id eq id }
      deletedRows > 0
    } catch (e: Exception) {
      false // The method returns false in case of an exception
    }
  }

  /**
   * Clears all points from the database.
   *
   * This method deletes all point records from the database.
   *
   * @return A [Result] containing `true` if the operation was successful, or a [DatabaseError] if an error occurs.
   * @throws DatabaseError If an error occurs during database operations.
   */
  override suspend fun clear(): Result<Boolean> = AppDB.exposedQuery {
    try {
      Points.deleteAll()
      Result.success(true)
    } catch (e: Exception) {
      Result.failure(DatabaseError()) // Changed to Result.failure
    }
  }
}