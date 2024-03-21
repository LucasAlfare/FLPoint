package com.lucasalfare.flpoint.server.models.errors

import kotlinx.serialization.Serializable

@Serializable
enum class RequestError : AppError {
  BadRequest,
  BadUrl
}