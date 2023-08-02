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
) {
  fun isValid(): Boolean {
    if (time == null) return false
    if (time!!.hour == 0 && time!!.minute == 0) return false
    return true
  }
}