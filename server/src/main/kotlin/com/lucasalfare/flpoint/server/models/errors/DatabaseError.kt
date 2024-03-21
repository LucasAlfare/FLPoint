package com.lucasalfare.flpoint.server.models.errors

import kotlinx.serialization.Serializable

@Serializable
enum class DatabaseError : AppError {
  AlreadyExists,
  NotFound,
  Internal
}