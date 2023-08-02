package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.model.PointRegistration
import com.lucasalfare.flpoint.server.service.MongoUsersService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId

fun Route.pointRegistrationRoute() {
  route("/api/users/registration") {
    post("/{id}") {
      val id = call.parameters["id"]
      if (id != null) {
        val data = call.receive<PointRegistration>()
        if (MongoUsersService.createPointRegistration(ObjectId(id), data)) {
          call.respond(HttpStatusCode.OK, "Point registration successfully created.")
        } else {
          call.respond(HttpStatusCode.BadRequest, "Can not create a point registration for you.")
        }
      } else {
        call.respond(HttpStatusCode.BadRequest, "Missing ID.")
      }
    }
  }
}