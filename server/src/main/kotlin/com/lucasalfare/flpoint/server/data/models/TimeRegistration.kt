package com.lucasalfare.flpoint.server.data.models

import kotlinx.serialization.Serializable

@Serializable
data class TimeRegistration(
  val dateTime: Long
)