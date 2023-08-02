package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.model.User
import com.lucasalfare.flpoint.server.service.MongoUsersService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUpRoute() {
  route("/signup") {
    post {
      val data = call.receive<User>()
      if (MongoUsersService.create(data)) {
        val nextToken = "" // TODO...
        call.respond(HttpStatusCode.OK, data.toCreatedUser(nextToken))
      } else {
        call.respond(HttpStatusCode.BadRequest, "Can not create your system user.")
      }
    }
  }
}