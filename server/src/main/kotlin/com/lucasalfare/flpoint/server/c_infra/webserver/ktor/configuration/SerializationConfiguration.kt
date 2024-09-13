package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

/**
 * Configures serialization settings for the application.
 *
 * This function installs the ContentNegotiation feature with JSON support.
 * It sets up the JSON parser with leniency disabled, meaning it will be strict
 * about parsing errors and only accept well-formed JSON data.
 */
fun Application.serializationConfiguration() {
  install(ContentNegotiation) {
    json(Json { isLenient = false })
  }
}