import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import com.lucasalfare.flpoint.server.a_domain.model.dto.BasicCredentialsDTO
import com.lucasalfare.flpoint.server.a_domain.model.dto.CreateUserDTO
import com.lucasalfare.flpoint.server.a_domain.model.dto.PointRequestDTO
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
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestPointRoutes {

  @BeforeTest
  fun setup() {
    runBlocking {
      MemoryUsersHandler.clear()
      MemoryPointsHandler.clear()
    }
  }

  @Test
  fun `test create point success route`() = testApplication {
    val c = setupTestClient()

    val resisterResponse = c.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(
        CreateUserDTO(
          name = "Lucas",
          email = "asdf@abc.com",
          plainPassword = "hehehe",
          targetRole = UserRole.Admin
        )
      )
    }

    val loginResponse = c.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(
        BasicCredentialsDTO(
          email = "asdf@abc.com",
          plainPassword = "hehehe"
        )
      )
    }

    val generatedJwt = loginResponse.body<String>()
    val createPointResponse = c.post("/point") {
      headers {
        append(HttpHeaders.Authorization, "Bearer $generatedJwt")
      }

      contentType(ContentType.Application.Json)

//      Instant.fromEpochMilliseconds(System.currentTimeMillis() - (10 * 60 * 1000)).toLocalDateTime(TimeZone.currentSystemDefault())
      val now = Clock.System.now()

      setBody(PointRequestDTO(timestamp = now))
    }

    assertEquals(
      expected = HttpStatusCode.Created,
      actual = createPointResponse.status
    )
  }

  @Test
  fun `test create point failure route`() = testApplication {
    val c = setupTestClient()

    val resisterResponse = c.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(
        CreateUserDTO(
          name = "Lucas",
          email = "asdf@abc.com",
          plainPassword = "hehehe",
          targetRole = UserRole.Admin
        )
      )
    }
    println("resisterResponse=${resisterResponse.bodyAsText()}")

    val loginResponse = c.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(
        BasicCredentialsDTO(
          email = "asdf@abc.com",
          plainPassword = "hehehe"
        )
      )
    }
    println("loginResponse=${loginResponse.bodyAsText()}")

    val generatedJwt = loginResponse.body<String>()

    c.post("/point") {
      headers {
        append(HttpHeaders.Authorization, "Bearer $generatedJwt")
      }
      contentType(ContentType.Application.Json)
      val now = Clock.System.now()
      setBody(PointRequestDTO(timestamp = now))
    }

    val createPointResponse = c.post("/point") {
      headers {
        append(HttpHeaders.Authorization, "Bearer $generatedJwt")
      }
      contentType(ContentType.Application.Json)
      val now = Clock.System.now()
      setBody(PointRequestDTO(timestamp = now))
    }

    assertEquals(
      expected = HttpStatusCode.UnprocessableEntity,
      actual = createPointResponse.status
    )
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