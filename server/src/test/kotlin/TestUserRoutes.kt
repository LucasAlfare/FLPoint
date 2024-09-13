import com.lucasalfare.flpoint.server.a_domain.model.UserRole
import com.lucasalfare.flpoint.server.a_domain.model.dto.BasicCredentialsDTO
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
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestUserRoutes {

  @BeforeTest
  fun setup() {
    runBlocking {
      MemoryUsersHandler.clear()
    }
  }

  @Test
  fun `test register route success`() = testApplication {
    val c = setupTestClient()

    val postResponse = c.post("/register") {
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

    assertEquals(
      expected = HttpStatusCode.Created,
      actual = postResponse.status
    )
  }

  @Test
  fun `test register route failure`() = testApplication {
    val c = setupTestClient()

    c.post("/register") {
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

    val postResponse = c.post("/register") {
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

    assertEquals(
      expected = HttpStatusCode.UnprocessableEntity,
      actual = postResponse.status
    )
  }

  @Test
  fun `test login route success`() = testApplication {
    val c = setupTestClient()

    c.post("/register") {
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

    assertEquals(
      expected = HttpStatusCode.OK,
      actual = loginResponse.status
    )
  }

  @Test
  fun `test login route failure`() = testApplication {
    val c = setupTestClient()

    c.post("/register") {
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
          plainPassword = "heheheeeee" // wrong plain pass
        )
      )
    }

    assertEquals(
      expected = HttpStatusCode.Unauthorized,
      actual = loginResponse.status
    )
  }

  @Test
  fun `test access protected route success`() = testApplication {
    val c = setupTestClient()

    c.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(
        CreateUserDTO(
          name = "Lucas",
          email = "asdf@abc.com",
          plainPassword = "hehehe",
          targetRole = UserRole.Standard
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
    val protectedAccessResponse = c.get("/protected") {
      headers {
        append(HttpHeaders.Authorization, "Bearer $generatedJwt")
      }
    }

    assertEquals(
      expected = HttpStatusCode.OK,
      actual = protectedAccessResponse.status
    )
  }

  @Test
  fun `test access protected route failure`() = testApplication {
    val c = setupTestClient()

    c.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(
        CreateUserDTO(
          name = "Lucas",
          email = "asdf@abc.com",
          plainPassword = "hehehe",
          targetRole = UserRole.Standard
        )
      )
    }

    val badJwt =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    val protectedAccessResponse = c.get("/protected") {
      headers {
        append(HttpHeaders.Authorization, "Bearer $badJwt")
      }
    }

    assertEquals(
      expected = HttpStatusCode.Unauthorized,
      actual = protectedAccessResponse.status
    )
  }

  @Test
  fun `test access protected for admin route success`() = testApplication {
    val c = setupTestClient()

    c.post("/register") {
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

    val protectedAcessResponse = c.get("/protected-for-admin") {
      headers {
        append(HttpHeaders.Authorization, "Bearer $generatedJwt")
      }
    }

    assertEquals(
      expected = HttpStatusCode.OK,
      actual = protectedAcessResponse.status
    )
  }

  @Test
  fun `test access protected for admin route failure`() = testApplication {
    val c = setupTestClient()

    c.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(
        CreateUserDTO(
          name = "Lucas",
          email = "asdf@abc.com",
          plainPassword = "hehehe",
          targetRole = UserRole.Standard // <-- not admin
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
    val protectedAccessResponse = c.get("/protected-for-admin") {
      headers {
        append(HttpHeaders.Authorization, "Bearer $generatedJwt")
      }
    }

    assertEquals(
      expected = HttpStatusCode.Unauthorized,
      actual = protectedAccessResponse.status
    )
  }
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