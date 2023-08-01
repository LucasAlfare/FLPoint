package com.lucasalfare.flpoint.server.model

import kotlinx.serialization.Serializable

@Serializable
enum class JustificationType {
  SickNote, Absence, None
}
