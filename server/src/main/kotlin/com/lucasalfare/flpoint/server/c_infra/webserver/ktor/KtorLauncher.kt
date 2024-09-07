package com.lucasalfare.flpoint.server.c_infra.webserver.ktor

import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.authenticationConfiguration
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.routingConfiguration
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.serializationConfiguration
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.statusPagesConfiguration
import io.ktor.server.engine.*
import io.ktor.server.netty.*

class KtorLauncher(
  val userUsecases: UserUsecases
) {
  fun launch() {
    embeddedServer(Netty, port = 7171) {
      serializationConfiguration()
      statusPagesConfiguration()
      authenticationConfiguration()
      routingConfiguration(userUsecases)
    }.start(true)
  }
}