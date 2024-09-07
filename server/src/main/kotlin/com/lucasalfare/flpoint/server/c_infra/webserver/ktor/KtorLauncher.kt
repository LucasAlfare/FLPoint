package com.lucasalfare.flpoint.server.c_infra.webserver.ktor

import com.lucasalfare.flpoint.server.a_domain.JwtGenerator
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import io.ktor.server.engine.*
import io.ktor.server.netty.*

class KtorLauncher(
  val userUsecases: UserUsecases,
  val jwtGenerator: JwtGenerator
) {
  fun launch() {
    embeddedServer(Netty, port = 7171) {
      configureSerialization()
      configureStatusPages()
      configureAuthentication(jwtGenerator)
      configureRouting(userUsecases)
    }.start(true)
  }
}