package com.lucasalfare.flpoint.server.models

data class Justification(
  val id: Long,
  val date: Long,
  val reason: String,
  val description: String,
  val relatedUserId: Long
)