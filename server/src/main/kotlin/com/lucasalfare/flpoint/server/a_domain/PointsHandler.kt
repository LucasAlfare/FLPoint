package com.lucasalfare.flpoint.server.a_domain

import com.lucasalfare.flpoint.server.a_domain.model.Point
import kotlinx.datetime.Instant

/**
 * Interface that defines a handler for managing point records in the system.
 *
 * The `PointsHandler` provides methods to create, retrieve, delete, and clear point records.
 */
interface PointsHandler {

  /**
   * Creates a new point record associated with a user.
   *
   * @param relatedUser The unique identifier of the user associated with the point record.
   * @param timestamp The exact time when the point was recorded.
   * @return A [Result] containing the ID of the newly created point record if successful, or an error otherwise.
   */
  suspend fun create(relatedUser: Int, timestamp: Instant): Result<Int>

  /**
   * Retrieves all point records in the system.
   *
   * @return A [Result] containing a list of all [Point] records, or an error if retrieval fails.
   */
  suspend fun get(): Result<List<Point>>

  /**
   * Retrieves all point records associated with a specific user.
   *
   * @param relatedUser The unique identifier of the user whose point records are to be retrieved.
   * @return A [Result] containing a list of [Point] records associated with the specified user, or an error otherwise.
   */
  suspend fun get(relatedUser: Int): Result<List<Point>>

  /**
   * Deletes a point record by its ID.
   *
   * @param id The unique identifier of the point record to be deleted.
   * @return `true` if the deletion was successful, `false` otherwise.
   */
  suspend fun delete(id: Int): Boolean

  /**
   * Clears all point records from the system.
   *
   * @return A [Result] indicating whether the operation to clear all records was successful.
   */
  suspend fun clear(): Result<Boolean>
}