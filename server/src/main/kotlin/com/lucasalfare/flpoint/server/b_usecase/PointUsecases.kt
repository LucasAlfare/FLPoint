package com.lucasalfare.flpoint.server.b_usecase

import com.lucasalfare.flpoint.server.a_domain.PointsHandler
import com.lucasalfare.flpoint.server.a_domain.model.UsecaseRuleError
import com.lucasalfare.flpoint.server.a_domain.model.dto.PointRequestDTO
import com.lucasalfare.flpoint.server.b_usecase.rule.PointUsecasesRules

class PointUsecases(
  var pointsHandler: PointsHandler
) {

  suspend fun createTimeRegistration(
    relatedUserId: Int,
    pointRequestDTO: PointRequestDTO
  ): Result<Int> {
    /*
    - we assume that request is pre-validated;
    - then:
      - get all registrations of request user;
      - if there are none, then just create;
      - otherwise, check rules:
        - request time is, at max, 1 minute away from server;
        - difference between last: must be >= 30 minutes;
      - if rules ok, then create registration;
      - otherwise,
     */
    val result = pointsHandler.get(relatedUserId)

    if (result.isSuccess) {
      val userPoints = result.getOrNull()
      if (userPoints != null) {
        if (userPoints.isNotEmpty()) {
          // TODO: logic!
          if (PointUsecasesRules.allPasses(last = userPoints.last().timestamp, check = pointRequestDTO.timestamp)) {
            throw UsecaseRuleError()
          }
        }

        return pointsHandler.create(relatedUserId, pointRequestDTO.timestamp)
      }
    }

    throw UsecaseRuleError()
  }
}