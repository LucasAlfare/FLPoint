package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.model.User
import com.lucasalfare.flpoint.server.service.MongoUsersService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId

fun Route.usersRoute() {
  route("/api/users") {
    post {
      val data = call.receive<User>()
      if (MongoUsersService.create(data)) {
        call.respond(HttpStatusCode.OK, data)
      } else {
        call.respond(HttpStatusCode.BadRequest)
      }
    }

    get {
      call.respond(HttpStatusCode.OK, MongoUsersService.getAll())
    }

    get("/{id}") {
      val userId = call.parameters["id"]
      if (userId != null) {
        val searchResult = MongoUsersService.getById(ObjectId(userId))

        if (searchResult != null) {
          call.respond(HttpStatusCode.OK, searchResult)
        } else {
          call.respond(HttpStatusCode.BadRequest)
        }
      }
    }

    patch("/{id}") {
      val userId = call.parameters["id"]
      if (userId != null) {
        val data = call.receive<User>()
        if (MongoUsersService.updateById(ObjectId(userId), data)) {
          call.respond(HttpStatusCode.OK)
        } else {
          call.respond(HttpStatusCode.BadRequest)
        }
      }
    }

    delete("/{id}") {
      val userId = call.parameters["id"]
      if (userId != null) {
        if (MongoUsersService.removeById(ObjectId(userId))) {
          call.respond(HttpStatusCode.OK)
        } else {
          call.respond(HttpStatusCode.BadRequest)
        }
      }
    }

    delete {
      MongoUsersService.clear()
      call.respond(HttpStatusCode.OK)
    }
  }
}