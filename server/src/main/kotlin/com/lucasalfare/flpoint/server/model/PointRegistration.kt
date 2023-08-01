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
import java.time.LocalTime

@Serializable
data class PointRegistration(
  var time: LocalTime? = null
)