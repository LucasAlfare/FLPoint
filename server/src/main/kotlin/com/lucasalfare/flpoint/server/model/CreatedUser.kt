@file:UseSerializers(
  ObjectIdSerializer::class
)

package com.lucasalfare.flpoint.server.model

import com.lucasalfare.flpoint.server.model.serializers.ObjectIdSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bson.types.ObjectId

@Serializable
data class CreatedUser(
  var id: ObjectId? = ObjectId(),
  var authToken: String? = null
)