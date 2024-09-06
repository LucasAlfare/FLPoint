package com.lucasalfare.flpoint.server

import com.lucasalfare.flpoint.server.data.AppDB
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
  AppDB.initialize(
    jdbcUrl = "",
    jdbcDriverClassName = "",
    username = "",
    password = "",
    maximumPoolSize = -1,
  ) {
    // TODO: create missing tables
  }

  embeddedServer(Netty, 3000) {
    configureAuthentication()
    configureSerialization()
    configureRouting()
  }.start(true)
}

fun Application.configureRouting() {
  routing {
  }
}

fun Application.configureSerialization() {
  install(ContentNegotiation) {
    json(Json { isLenient = false })
  }
}

fun Application.configureAuthentication() {
  install(Authentication) {
    jwt("TODO") {

    }
  }
}