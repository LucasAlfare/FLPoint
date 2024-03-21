package com.lucasalfare.flpoint.server.models.errors

enum class BusinessError : AppError {

  NotAllowed,
  TimeRegistrationExceedsLimitDelay,
  TimeRegistration
}