package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.serializationConfiguration() {
  install(ContentNegotiation) {
    json(Json { isLenient = false })
  }
}