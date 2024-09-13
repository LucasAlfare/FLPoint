package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration

import com.lucasalfare.flpoint.server.b_usecase.PointUsecases
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.admin.adminPointsCrudRoutes
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.dummy.protectedForAdminRoute
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.dummy.protectedRoute
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.point.pointsHandlingRoutes
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.user.userAccessRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.routingConfiguration(
  userUsecases: UserUsecases,
  pointUsecases: PointUsecases
) {
  routing {
    protectedRoute()
    protectedForAdminRoute()
    userAccessRoutes(userUsecases)
    pointsHandlingRoutes(pointUsecases)
    adminPointsCrudRoutes(pointUsecases)
  }
}