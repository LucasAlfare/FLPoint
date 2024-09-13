package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration

import com.lucasalfare.flpoint.server.b_usecase.PointUsecases
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.*
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.admin.adminPointsCrudRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.routingConfiguration(
  userUsecases: UserUsecases,
  pointUsecases: PointUsecases
) {
  routing {
    protectedRoute()
    protectedForAdminRoute()
    registerPostRoute(userUsecases)
    loginPostRoute(userUsecases)
    pointRoute(pointUsecases)
    listPointsRoute(pointUsecases)
    adminPointsCrudRoutes(pointUsecases)
  }
}