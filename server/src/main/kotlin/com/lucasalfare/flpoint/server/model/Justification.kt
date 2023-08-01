@file:UseSerializers(
  LocalTimeSerializer::class,
  DateSerializer::class,
  UUIDSerializer::class
)

package com.lucasalfare.flpoint.server.model

import com.lucasalfare.flpoint.server.model.serializers.DateSerializer
import com.lucasalfare.flpoint.server.model.serializers.LocalTimeSerializer
import com.lucasalfare.flpoint.server.model.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

@Serializable
data class Justification(
  var justificationType: JustificationType? = JustificationType.None,
  var date: Date? = Date(),
  var details: String? = ""
)