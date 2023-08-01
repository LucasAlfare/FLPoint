package com.lucasalfare.flpoint.server.model

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(
  var username: String? = null,
  var password: String? = null
)