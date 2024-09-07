package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration

import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.loginPostRoute
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.protectedForAdminRoute
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.protectedRoute
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.registerPostRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.routingConfiguration(userUsecases: UserUsecases) {
  routing {
    protectedRoute()
    protectedForAdminRoute()
    registerPostRoute(userUsecases)
    loginPostRoute(userUsecases)
  }
}