package com.lucasalfare.flpoint.server.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Long, val login: String, val hashedPassword: String)