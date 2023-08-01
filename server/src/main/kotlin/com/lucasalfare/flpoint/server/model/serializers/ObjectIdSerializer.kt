package com.lucasalfare.flpoint.server.model.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.types.ObjectId

object ObjectIdSerializer : KSerializer<ObjectId> {
  override val descriptor = PrimitiveSerialDescriptor(
    "org.bson.types.ObjectId", PrimitiveKind.STRING
  )

  override fun serialize(encoder: Encoder, value: ObjectId) =
    encoder.encodeString(value.toHexString())

  override fun deserialize(decoder: Decoder) =
    ObjectId(decoder.decodeString())
}