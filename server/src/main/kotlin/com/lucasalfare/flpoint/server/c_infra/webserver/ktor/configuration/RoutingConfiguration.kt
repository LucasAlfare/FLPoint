package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration

import com.lucasalfare.flpoint.server.b_usecase.TimeRegistrationUseCases
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.routingConfiguration(
  userUsecases: UserUsecases,
  timeRegistrationUseCases: TimeRegistrationUseCases
) {
  routing {
    protectedRoute()
    protectedForAdminRoute()
    registerPostRoute(userUsecases)
    loginPostRoute(userUsecases)
    clockInRoute(timeRegistrationUseCases)
  }
}