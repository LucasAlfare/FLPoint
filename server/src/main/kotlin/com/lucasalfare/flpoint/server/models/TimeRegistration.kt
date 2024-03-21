package com.lucasalfare.flpoint.server.models

import kotlinx.serialization.Serializable

@Serializable
data class TimeRegistration(
  val dateTime: Long
)