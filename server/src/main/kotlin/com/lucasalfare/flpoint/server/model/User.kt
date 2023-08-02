@file:UseSerializers(
  LocalTimeSerializer::class,
  DateSerializer::class,
  ObjectIdSerializer::class
)

package com.lucasalfare.flpoint.server.model

import com.lucasalfare.flpoint.server.model.serializers.DateSerializer
import com.lucasalfare.flpoint.server.model.serializers.LocalTimeSerializer
import com.lucasalfare.flpoint.server.model.serializers.ObjectIdSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bson.types.ObjectId
import java.time.LocalTime

@Serializable
data class User(
  @SerialName("_id")
  var id: ObjectId? = ObjectId(),
  var credentials: Credentials? = null,
  var enterTimes: MutableList<LocalTime>? = null,
  var exitTimes: MutableList<LocalTime>? = null,
  var maxAuthenticationsPerDay: Int? = null,
  var pointRegistrations: MutableList<PointRegistration>? = null,
  var justifications: MutableList<Justification>? = null
) {
  fun toCreatedUser(nextToken: String? = null) = CreatedUser(id, nextToken)
}