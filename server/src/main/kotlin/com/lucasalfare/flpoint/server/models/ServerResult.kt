package com.lucasalfare.flpoint.server.models

import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ServerResult(
  @Contextual val code: HttpStatusCode,
  @Contextual val data: Any? = null
)