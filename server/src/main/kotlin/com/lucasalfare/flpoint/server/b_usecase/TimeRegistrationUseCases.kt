package com.lucasalfare.flpoint.server.b_usecase

import com.lucasalfare.flpoint.server.a_domain.TimeRegistrationsHandler
import com.lucasalfare.flpoint.server.a_domain.model.dto.ClockInRequestDTO

class TimeRegistrationUseCases(
  var timeRegistrationsHandler: TimeRegistrationsHandler,
  // var usersHandler: UsersHandler // <-- really needed?
) {

  suspend fun createTimeRegistration(clockInRequestDTO: ClockInRequestDTO): Result<Int> {
    // TODO: validations
    return timeRegistrationsHandler.create(clockInRequestDTO.userId, clockInRequestDTO.timestamp)
  }
}