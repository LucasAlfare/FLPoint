package com.lucasalfare.flpoint.server.routes

import com.lucasalfare.flpoint.server.data.services.Users
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.protected() {
  authenticate("my-jwt-auth") {
    get("flpoint/users/{id}/protected") {
      call.parameters["id"]?.let {
        val result = Users.getUserById(it.toLong())
        return@get call.respond(result.code, "Your SECRET data:\n[${result.data ?: "NOTHING FOUND!"}]")
      }

      return@get call.respond("Was not possible parse the ID from the URL but you are still seeing a PROTECTED route.")
    }
  }
}