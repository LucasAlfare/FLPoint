package com.lucasalfare.flpoint.server.b_usecase

import com.lucasalfare.flpoint.server.a_domain.PointsHandler
import com.lucasalfare.flpoint.server.a_domain.model.UsecaseRuleError
import com.lucasalfare.flpoint.server.a_domain.model.dto.CreatePointRequestDTO
import com.lucasalfare.flpoint.server.a_domain.model.dto.PointsDTO
import com.lucasalfare.flpoint.server.b_usecase.rule.PointUsecasesRules

class PointUsecases(
  var pointsHandler: PointsHandler
) {

  suspend fun createTimeRegistration(
    relatedUserId: Int,
    createPointRequestDTO: CreatePointRequestDTO
  ): Result<Int> {
    /*
    - we assume that request body fields is pre-validated;
    - then:
      - get all registrations of request user;
      - if there are none, then just create;
      - otherwise, check rules:
        - request time is, at max, 1 minute away from server;
        - difference between last: must be >= 30 minutes;
      - if rules ok, then create registration;
      - otherwise, throw error
     */
    val result = pointsHandler.get(relatedUserId)

    if (result.isSuccess) {
      val userPoints = result.getOrNull()
      if (userPoints != null) {
        if (userPoints.isNotEmpty()) {
          // TODO: logic!
          if (
            !PointUsecasesRules.allPasses(last = userPoints.last().timestamp, check = createPointRequestDTO.timestamp)
          ) {
            throw UsecaseRuleError()
          }
        }

        return pointsHandler.create(relatedUserId, createPointRequestDTO.timestamp)
      }
    }

    throw UsecaseRuleError()
  }

  suspend fun getAllUserPoints(relatedUserId: Int): PointsDTO {
    val search = pointsHandler.get(relatedUserId)
    if (search.isSuccess) {
      return PointsDTO(timestamps = search.getOrNull()!!.map { it.timestamp })
    }

    return PointsDTO(emptyList())
  }
}