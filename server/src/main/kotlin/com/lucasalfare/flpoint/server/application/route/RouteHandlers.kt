package com.lucasalfare.flpoint.server.application.route

import com.lucasalfare.flpoint.server.domain.model.CredentialsDTO
import com.lucasalfare.flpoint.server.domain.model.UpdateUserPasswordRequestDTO
import com.lucasalfare.flpoint.server.domain.model.CreateUserRequestDTO
import com.lucasalfare.flpoint.server.application.usecase.AppUsecases
import com.lucasalfare.flpoint.server.infrastructure.security.JwtUtils.getAppJwtClaims
import com.lucasalfare.flpoint.server.infrastructure.security.JwtUtils.getBearerToken
import com.lucasalfare.flpoint.server.infrastructure.security.handleAsAuthorizedAdmin
import com.lucasalfare.flpoint.server.shared.AppError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.routesHandlers() {
  //<editor-fold desc="PUBLIC-ROUTES">
  // global root health route
  get("/health") { call.respondText("Hello from Kotlin/Ktor API!") }

  // used for logging in an existing user
  post("/login") {
    val dto = call.receive<CredentialsDTO>()
    val result = AppUsecases.loginUser(dto)
    return@post call.respond(HttpStatusCode.OK, result)
  }
  //</editor-fold>

  //<editor-fold desc="UNDER-AUTH-ROUTES">
  authenticate("flpoint-jwt-auth") {
    //<editor-fold desc="USER-ROUTES">
    // used to update current password
    patch("/user/update-password") {
      val claims = call.getAppJwtClaims() ?: throw AppError("Error retrieving JWT claims!")
      val receivedCurrentPlainPassword = call.receive<UpdateUserPasswordRequestDTO>()
      val result = AppUsecases.updateUserPassword(
        userId = claims.userId,
        currentPlainPassword = receivedCurrentPlainPassword.currentPlainPassword,
        newPlainPassword = receivedCurrentPlainPassword.newPlainPassword
      )

      return@patch call.respond(HttpStatusCode.OK, result)
    }

    // used to create point
    post("/user/point") {
      val claims = call.getAppJwtClaims() ?: throw AppError("Error retrieving JWT claims!")
      val result = AppUsecases.doPoint(claims.userId)
      return@post call.respond(HttpStatusCode.Created, result)
    }

    // TODO: make this cache results
    // Route for user getting only his own points
    get("/user/points") {
      val claims = call.getAppJwtClaims() ?: throw AppError("Error retrieving JWT claims!")
      val result = AppUsecases.getUserPoints(claims.userId)
      return@get call.respond(HttpStatusCode.OK, result)
    }

    post("/user/logout") {
      val jwt = call.getBearerToken()
        ?: throw AuthenticationError("Missing Authorization token")

      AppUsecases.logoutUser(jwt)

      return@post call.respond(HttpStatusCode.OK)
    }
    //</editor-fold>

    //<editor-fold desc="ADMIN-ONLY-ROUTES">
    // admin health route
    get("/admin/health") {
      return@get handleAsAuthorizedAdmin {
        call.respond(HttpStatusCode.OK)
      }
    }

    // used for signup a user
    post("/admin/register") {
      return@post handleAsAuthorizedAdmin {
        val dto = call.receive<CreateUserRequestDTO>()
        val result = AppUsecases.signupUser(dto)
        call.respond(status = HttpStatusCode.Created, message = result)
      }
    }

    // TODO: make this cache results
    // used to get all database users
    get("/admin/users") {
      return@get handleAsAuthorizedAdmin {
        val result = AppUsecases.getAllAppUsers()
        return@handleAsAuthorizedAdmin call.respond(HttpStatusCode.OK, result)
      }
    }

    // used to delete the {id} user
    delete("/admin/users/{id}") {
      return@delete handleAsAuthorizedAdmin {
        val userId = call.parameters["id"] ?: throw AppError("Missing user ID")
        val result = AppUsecases.deleteUser(userId.toInt())
        return@handleAsAuthorizedAdmin call.respond(HttpStatusCode.OK, result)
      }
    }

    // TODO: make this cache results
    // used to retrieve all the points of the database
    get("/admin/points") {
      return@get handleAsAuthorizedAdmin {
        val result = AppUsecases.getAllAppPoints()
        return@handleAsAuthorizedAdmin call.respond(HttpStatusCode.OK, result)
      }
    }
    //</editor-fold>
  }
  //</editor-fold>
}
