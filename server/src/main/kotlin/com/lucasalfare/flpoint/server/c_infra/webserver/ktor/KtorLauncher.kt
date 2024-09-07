package com.lucasalfare.flpoint.server.c_infra.webserver.ktor

import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import io.ktor.server.engine.*
import io.ktor.server.netty.*

class KtorLauncher(
  val userUsecases: UserUsecases
) {
  fun launch() {
    embeddedServer(Netty, port = 7171) {
      configureSerialization()
      configureStatusPages()
      configureAuthentication()
      configureRouting(userUsecases)
    }.start(true)
  }
}