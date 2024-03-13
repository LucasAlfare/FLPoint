package com.lucasalfare.flpoint.server

import com.lucasalfare.flpoint.server.data.MyDatabase
import com.lucasalfare.flpoint.server.routes.signup
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun main() {
  MyDatabase.initialize(
    username = "FLPointUsername",
    password = "FLPointPassword"
  )

  embeddedServer(Netty, 3000) {
    configureSerialization()
    configureRouting()
  }.start(true)
}

fun Application.configureRouting() {
  routing {
    /*
    - The client sends a JSON with login and password (original, not hashed);
    - The server responds with:
      - The code OK and with the just created user ID ([Long]) when everything works;
      - The code NotAcceptable and the error messages when something goes wrong.

    Example request:
    curl -d '{"login":"abc@def.com", "password":"original_password"}' -H "Content-Type: application/json" -X POST http://localhost:3000/flpoint/user
     */
    signup()
  }
}

fun Application.configureSerialization() {
  install(ContentNegotiation) {
    json()
  }
}

fun Throwable.toErrorResponseString(): String {
  val t = this
  return buildString {
    if (t.message != null) append("Message:\n\t${t.message}\n")
    if (t.cause != null) append("Cause:\n\t${t.cause}\n")
  }
}