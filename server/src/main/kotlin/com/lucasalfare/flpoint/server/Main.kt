package com.lucasalfare.flpoint.server

import com.lucasalfare.flpoint.server.c_infra.data.exposed.ExposedInitializer
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.KtorLauncher

fun main() {
  ExposedInitializer.initialize()
  KtorLauncher.launch()
}