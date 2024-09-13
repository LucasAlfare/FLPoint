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

/**
 * Configures the routing for the application.
 *
 * This function sets up various routes for the application, including:
 * - Protected routes that require authentication.
 * - Routes specifically for administrators.
 * - User access routes for handling user-related actions.
 * - Point handling routes for managing point-related actions.
 * - Admin-specific routes for CRUD operations on points.
 *
 * @param userUsecases The use cases related to user operations, such as user registration and authentication.
 * @param pointUsecases The use cases related to point operations, including point registration and management.
 */
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