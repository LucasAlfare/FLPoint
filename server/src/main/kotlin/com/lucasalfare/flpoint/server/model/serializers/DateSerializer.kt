package com.lucasalfare.flpoint.server.model.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

object DateSerializer : KSerializer<Date> {
  override val descriptor = PrimitiveSerialDescriptor(
    "java.util.Date", PrimitiveKind.STRING
  )

  override fun serialize(encoder: Encoder, value: Date) =
    encoder.encodeString(value.toString())

  override fun deserialize(decoder: Decoder): Date =
    SimpleDateFormat("dd-mm-YYYY").parse(decoder.decodeString())
}