package com.lucasalfare.flpoint.server.c_infra.webserver.ktor

import io.ktor.server.engine.*
import io.ktor.server.netty.*

object KtorLauncher {

  fun launch() {
    embeddedServer(Netty, 7171) {

    }.start(true)
  }
}