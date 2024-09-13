package route.admin

import com.lucasalfare.flpoint.server.a_domain.model.Point
import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import com.lucasalfare.flpoint.server.a_domain.model.dto.BasicCredentialsDTO
import com.lucasalfare.flpoint.server.a_domain.model.dto.CreatePointRequestDTO
import com.lucasalfare.flpoint.server.a_domain.model.dto.CreateUserDTO
import com.lucasalfare.flpoint.server.b_usecase.PointUsecases
import com.lucasalfare.flpoint.server.b_usecase.UserUsecases
import com.lucasalfare.flpoint.server.c_infra.data.memory.MemoryPointsHandler
import com.lucasalfare.flpoint.server.c_infra.data.memory.MemoryUsersHandler
import com.lucasalfare.flpoint.server.c_infra.security.hashing.dummy.DummyPasswordHasher
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.authenticationConfiguration
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.routingConfiguration
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.serializationConfiguration
import com.lucasalfare.flpoint.server.c_infra.webserver.ktor.configuration.statusPagesConfiguration
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestAdminCrud {

  @BeforeTest
  fun setup() {
    runBlocking {
      MemoryUsersHandler.clear()
      MemoryPointsHandler.clear()
    }
  }

  @Test
  fun `test admin get all application points success`() = testApplication {
    val c = setupTestClient()

    // prepare dummy users actions
    repeat(2) {
      c.post("/register") {
        contentType(ContentType.Application.Json)
        setBody(
          CreateUserDTO(
            name = "Lucas$it",
            email = "asdf$it@abc.com",
            plainPassword = "hehehe",
            targetRole = UserRole.Standard
          )
        )
      }

      val generatedJwt = c.post("/login") {
        contentType(ContentType.Application.Json)
        setBody(
          BasicCredentialsDTO(
            email = "asdf$it@abc.com",
            plainPassword = "hehehe"
          )
        )
      }.body<String>()
      c.post("/point") {
        headers {
          append(HttpHeaders.Authorization, "Bearer $generatedJwt")
        }
        contentType(ContentType.Application.Json)
        val now = Clock.System.now()
        setBody(CreatePointRequestDTO(timestamp = now))
      }
    }

    // prepare admin signup and jwt
    c.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(
        CreateUserDTO(
          name = "Lucas Admin",
          email = "asdf@abc.com",
          plainPassword = "hehehe",
          targetRole = UserRole.Admin
        )
      )
    }

    val adminJwt = c.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(
        BasicCredentialsDTO(
          email = "asdf@abc.com",
          plainPassword = "hehehe"
        )
      )
    }.body<String>()

    // performs request to role-protected route
    val adminGetPointsResponse = c.get("/admin/points") {
      headers {
        append(HttpHeaders.Authorization, "Bearer $adminJwt")
      }
    }

    assertEquals(
      expected = HttpStatusCode.OK,
      actual = adminGetPointsResponse.status
    )

    val receivedPoints = adminGetPointsResponse.body<List<Point>>()
    assertTrue(receivedPoints.size == 2)
  }

  @Test
  fun `test admin delete point success`() = testApplication {
    val c = setupTestClient()

    // prepare dummy users actions
    repeat(2) {
      c.post("/register") {
        contentType(ContentType.Application.Json)
        setBody(
          CreateUserDTO(
            name = "Lucas$it",
            email = "asdf$it@abc.com",
            plainPassword = "hehehe",
            targetRole = UserRole.Standard
          )
        )
      }

      val generatedJwt = c.post("/login") {
        contentType(ContentType.Application.Json)
        setBody(
          BasicCredentialsDTO(
            email = "asdf$it@abc.com",
            plainPassword = "hehehe"
          )
        )
      }.body<String>()
      c.post("/point") {
        headers {
          append(HttpHeaders.Authorization, "Bearer $generatedJwt")
        }
        contentType(ContentType.Application.Json)
        val now = Clock.System.now()
        setBody(CreatePointRequestDTO(timestamp = now))
      }
    }

    // prepare admin signup and jwt
    c.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(
        CreateUserDTO(
          name = "Lucas Admin",
          email = "admin@abc.com",
          plainPassword = "hehehe",
          targetRole = UserRole.Admin
        )
      )
    }

    val adminJwt = c.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(
        BasicCredentialsDTO(
          email = "admin@abc.com",
          plainPassword = "hehehe"
        )
      )
    }.body<String>()

    // performs request to role-protected route
    var adminGetPointsResponse = c.get("/admin/points") {
      headers {
        append(HttpHeaders.Authorization, "Bearer $adminJwt")
      }
    }

    var receivedPoints = adminGetPointsResponse.body<List<Point>>()
    val idOfAReceivedPoint = receivedPoints.first().id

    // performs request to target testing route
    val adminDeletePointResponse = c.delete("/admin/points/$idOfAReceivedPoint") {
      headers {
        append(HttpHeaders.Authorization, "Bearer $adminJwt")
      }
    }

    // checks status
    assertEquals(
      expected = HttpStatusCode.OK,
      actual = adminDeletePointResponse.status
    )

    // get all again...
    adminGetPointsResponse = c.get("/admin/points") {
      headers {
        append(HttpHeaders.Authorization, "Bearer $adminJwt")
      }
    }
    receivedPoints = adminGetPointsResponse.body<List<Point>>()

    // ...and checks size
    assertTrue(receivedPoints.size == 1)
  }

  private fun ApplicationTestBuilder.setupTestClient(): HttpClient {
    application {
      authenticationConfiguration()
      serializationConfiguration()
      statusPagesConfiguration()
      routingConfiguration(
        userUsecases = UserUsecases(
          usersHandler = MemoryUsersHandler,
          passwordHasher = DummyPasswordHasher
        ),
        pointUsecases = PointUsecases(
          MemoryPointsHandler
        )
      )
    }

    return createClient {
      install(ContentNegotiation) {
        json(Json { isLenient = false })
      }
    }
  }
}