package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.healthRoute() {
  get {
    call.respondText("Hello from Ktor! We are healthy!")
  }
}