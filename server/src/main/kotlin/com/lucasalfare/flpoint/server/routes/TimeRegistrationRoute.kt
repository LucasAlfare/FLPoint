package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.security.DEFAULT_JWT_CONFIG
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.timeRegistration() {
  authenticate(DEFAULT_JWT_CONFIG) {

  }
}