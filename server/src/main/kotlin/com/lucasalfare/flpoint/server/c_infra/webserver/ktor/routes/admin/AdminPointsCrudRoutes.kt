package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.admin

import com.lucasalfare.flpoint.server.b_usecase.PointUsecases
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.handleAsAuthenticatedAdmin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configures routes for administrative CRUD operations on points.
 *
 * This function sets up routes for managing points that require authentication as an admin.
 * It includes:
 * - A GET route at `/admin/points` for retrieving all points of all users. The route ensures
 *   the request is authenticated as an admin, then calls `pointUsecases.getAllPointsOfAllUsers`
 *   to fetch the points and responds with HTTP 200 OK and the result of the operation.
 * - A DELETE route at `/admin/points/{id}` for deleting a specific point by ID. The route ensures
 *   the request is authenticated as an admin, retrieves the point ID from the path parameters,
 *   performs the deletion via `pointUsecases.deletePoint`, and responds with HTTP 200 OK if successful.
 *
 * @param pointUsecases The use cases related to point operations, such as retrieving and deleting points.
 */
fun Routing.adminPointsCrudRoutes(pointUsecases: PointUsecases) {
  authenticate("flpoint-jwt-auth") {
    get("/admin/points") {
      return@get handleAsAuthenticatedAdmin {
        val result = pointUsecases.getAllPointsOfAllUsers()
        call.respond(HttpStatusCode.OK, result.getOrThrow())
      }
    }

    delete("/admin/points/{id}") {
      return@delete handleAsAuthenticatedAdmin {
        val id = call.parameters["id"]!!.toInt()
        if (pointUsecases.deletePoint(id)) {
          call.respond(HttpStatusCode.OK)
        }
      }
    }
  }
}