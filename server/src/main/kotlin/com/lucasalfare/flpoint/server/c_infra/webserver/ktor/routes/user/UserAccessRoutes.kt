package com.lucasalfare.flpoint.server.c_infra.webserver.ktor.routes.user

import com.lucasalfare.flpoint.server.a_domain.model.dto.BasicCredentialsDTO
import com.lucasalfare.flpoint.server.a_domain.model.dto.CreateUserDTO
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configures user access routes that do not require authentication.
 *
 * This function sets up routes related to user operations that are accessible without
 * authentication. It includes:
 * - A POST route at `/login` for user login, where it receives `BasicCredentialsDTO`,
 *   performs the login via `userUsecases.loginUser(dto)`, and responds with the generated JWT.
 * - A POST route at `/register` for user registration, where it receives `CreateUserDTO`,
 *   creates a user via `userUsecases.createUser(dto)`, and responds with the created user data.
 *
 * @param userUsecases The use cases related to user operations, such as login and registration.
 */
fun Routing.userAccessRoutes(userUsecases: UserUsecases) {
  post("/login") {
    val dto = call.receive<BasicCredentialsDTO>()
    val jwt = userUsecases.loginUser(dto)
    call.respond(HttpStatusCode.OK, jwt)
  }

  post("/register") {
    val dto = call.receive<CreateUserDTO>()
    val result = userUsecases.createUser(dto)
    call.respond(HttpStatusCode.Created, result.getOrNull()!!)
  }
}