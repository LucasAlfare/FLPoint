package com.lucasalfare.flpoint.server.b_usecase

import com.lucasalfare.flpoint.server.a_domain.PointsHandler
import com.lucasalfare.flpoint.server.a_domain.model.Point
import com.lucasalfare.flpoint.server.a_domain.model.UsecaseRuleError
import com.lucasalfare.flpoint.server.a_domain.model.dto.CreatePointRequestDTO
import com.lucasalfare.flpoint.server.b_usecase.rule.PointUsecasesRules

/**
 * Class that handles the use cases for managing point records.
 *
 * The `PointUsecases` class provides methods to create, retrieve, and delete point records,
 * applying business rules and validations as necessary.
 *
 * @property pointsHandler The handler responsible for performing operations on point records.
 */
class PointUsecases(
  var pointsHandler: PointsHandler
) {

  /**
   * Creates a new time registration (point) for a specific user.
   *
   * The method performs the following steps:
   * - Assumes that the request body fields are pre-validated.
   * - Retrieves all existing point records for the specified user.
   * - If no existing records are found, creates a new point record.
   * - If existing records are found, checks the following rules:
   *   - The request timestamp must be within 1 minute of the server's current time.
   *   - There must be at least a 30-minute gap between the last recorded point and the new point.
   * - If both rules are satisfied, creates the new point record.
   * - If any rule is violated, throws a `UsecaseRuleError`.
   *
   * @param relatedUserId The unique identifier of the user for whom the point is being created.
   * @param createPointRequestDTO The data transfer object containing the timestamp for the new point.
   * @return A [Result] containing the ID of the newly created point if successful, or an error otherwise.
   * @throws UsecaseRuleError If any of the validation rules are not met.
   */
  suspend fun createTimeRegistration(
    relatedUserId: Int,
    createPointRequestDTO: CreatePointRequestDTO
  ): Result<Int> {
    val result = pointsHandler.get(relatedUserId)

    if (result.isSuccess) {
      val userPoints = result.getOrNull()
      if (userPoints != null) {
        if (userPoints.isNotEmpty()) {
          if (
            !PointUsecasesRules.isWithinValidTimeRange(createPointRequestDTO.timestamp) ||
            !PointUsecasesRules.passedAtLeast30MinFromLast(userPoints.last().timestamp, createPointRequestDTO.timestamp)
          ) {
            throw UsecaseRuleError()
          }
        }

        return pointsHandler.create(relatedUserId, createPointRequestDTO.timestamp)
      }
    }

    throw UsecaseRuleError()
  }

  /**
   * Retrieves all point records for a specific user.
   *
   * @param relatedUserId The unique identifier of the user whose points are to be retrieved.
   * @return A list of [Point] records associated with the specified user, or an empty list if retrieval fails.
   */
  suspend fun getAllUserPoints(relatedUserId: Int): List<Point> {
    val search = pointsHandler.get(relatedUserId)
    if (search.isSuccess) {
      return search.getOrElse { emptyList() }
    }

    return emptyList()
  }

  /**
   * Retrieves all point records across all users.
   *
   * @return A [Result] containing a list of all [Point] records, or an error if retrieval fails.
   */
  suspend fun getAllPointsOfAllUsers(): Result<List<Point>> {
    return pointsHandler.get()
  }

  /**
   * Deletes a specific point record by its ID.
   *
   * @param id The unique identifier of the point record to be deleted.
   * @return `true` if the deletion was successful, `false` otherwise.
   */
  suspend fun deletePoint(id: Int): Boolean {
    return pointsHandler.delete(id)
  }
}