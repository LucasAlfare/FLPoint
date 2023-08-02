package com.lucasalfare.flpoint.server

import com.lucasalfare.flpoint.server.routes.loginRoute
import com.lucasalfare.flpoint.server.routes.pointRegistrationRoute
import com.lucasalfare.flpoint.server.routes.signUpRoute
import com.lucasalfare.flpoint.server.routes.usersRoute
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*


fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
  configureSerialization()
  configureRouting()
}

fun Application.configureRouting() {
  routing {
    usersRoute()
    signUpRoute()
    loginRoute()
    pointRegistrationRoute()
  }
}

fun Application.configureSerialization() {
  install(ContentNegotiation) {
    json()
  }
}