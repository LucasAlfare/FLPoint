package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.model.Credentials
import com.lucasalfare.flpoint.server.security.checkPassword
import com.lucasalfare.flpoint.server.service.MongoUsersService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId

fun Route.loginRoute() {
  route("/login") {
    post("{id}") {
      val id = call.parameters["id"]
      if (id != null) {
        val searchUser = MongoUsersService.getById(ObjectId(id))
        if (searchUser != null) {
          val data = call.receive<Credentials>()
          if (searchUser.credentials!!.checkPassword(data.password!!)) {
            val nextToken = "bilu teteia!!" // TODO: generate new JWT TOKEN here...
            call.respond(HttpStatusCode.OK, nextToken)
          } else {
            call.respond(HttpStatusCode.BadRequest, "Invalid credentials. Try again later.")
          }
        } else {
          call.respond(HttpStatusCode.BadRequest, "User doesn't exists.")
        }
      } else {
        call.respond(HttpStatusCode.BadRequest, "Missing ID. Have you ever signed up to the system?")
      }
    }
  }
}