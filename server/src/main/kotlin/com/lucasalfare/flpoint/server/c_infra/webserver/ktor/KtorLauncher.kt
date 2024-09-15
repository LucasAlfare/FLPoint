package com.lucasalfare.flpoint.server.c_infra.webserver.ktor

import com.lucasalfare.flpoint.server.a_domain.EnvsLoader.loadEnv
import com.lucasalfare.flpoint.server.b_usecase.PointUsecases
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.authenticationConfiguration
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.routingConfiguration
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.serializationConfiguration
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.statusPagesConfiguration
import io.ktor.server.engine.*
import io.ktor.server.netty.*

/**
 * Launches the Ktor server with specified configurations and use cases.
 *
 * This class sets up and starts an embedded Ktor server using the Netty engine.
 * It configures the server with serialization, status pages, authentication, and routing settings.
 *
 * @property userUsecases The use cases related to user operations, which will be used in routing configuration.
 * @property pointUsecases The use cases related to point operations, which will be used in routing configuration.
 */
class KtorLauncher(
  private val userUsecases: UserUsecases,
  private val pointUsecases: PointUsecases
) {
  /**
   * Starts the Ktor server with the configured settings.
   *
   * This function initializes the Ktor server on port 7171 and applies the following configurations:
   * - Serialization configuration for JSON processing.
   * - Status pages configuration for handling exceptions.
   * - Authentication configuration for securing routes.
   * - Routing configuration to set up application routes with provided use cases.
   */
  fun launch() {
    val webserverPort = loadEnv("WEBSERVER_PORT", throwWhenNull = true, throwWhenEmpty = true)

    embeddedServer(
      Netty,
      port = webserverPort.toInt()
    ) {
      serializationConfiguration()
      statusPagesConfiguration()
      authenticationConfiguration()
      routingConfiguration(
        userUsecases,
        pointUsecases
      )
    }.start(true)
  }
}