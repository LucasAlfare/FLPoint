package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.point

import com.lucasalfare.flpoint.server.a_domain.model.dto.CreatePointRequestDTO
import com.lucasalfare.flpoint.server.b_usecase.PointUsecases
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configures routes for handling points that require authentication.
 *
 * This function sets up routes related to point operations that require user authentication.
 * It includes:
 * - A POST route at `/point` for creating a point registration. The route extracts the user ID
 *   from the JWT principal, receives a `CreatePointRequestDTO`, and uses `pointUsecases.createTimeRegistration`
 *   to create the point registration. It responds with HTTP 201 Created and the result of the operation.
 * - A GET route at `/points` for retrieving all points for the authenticated user. The route extracts
 *   the user ID from the JWT principal, calls `pointUsecases.getAllUserPoints`, and responds with HTTP 200 OK
 *   and the list of points.
 *
 * @param pointUsecases The use cases related to point operations, such as creating and retrieving points.
 */
fun Routing.pointsHandlingRoutes(pointUsecases: PointUsecases) {
  authenticate("flpoint-jwt-auth") {
    post("/point") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal?.payload?.getClaim("id")?.asInt() ?: -1
      val req = call.receive<CreatePointRequestDTO>()
      val result = pointUsecases.createTimeRegistration(userId, req)
      return@post call.respond(HttpStatusCode.Created, result.getOrNull()!!)
    }

    get("/points") {
      val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
      val result = pointUsecases.getAllUserPoints(userId)
      return@get call.respond(HttpStatusCode.OK, result)
    }
  }
}