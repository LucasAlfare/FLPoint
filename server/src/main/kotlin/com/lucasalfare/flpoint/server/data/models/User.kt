package com.lucasalfare.flpoint.server.data.models

data class User(val id: Long, val login: String, val hashedPassword: String)