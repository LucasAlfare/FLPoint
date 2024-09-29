import com.lucasalfare.flpoint.server.CreateUserRequestDTO
import com.lucasalfare.flpoint.server.CredentialsDTO
import com.lucasalfare.flpoint.server.InMemoryDataHandler
import com.lucasalfare.flpoint.server.TimeInterval
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import kotlin.test.*

class UserFlowTest {

  @AfterTest
  fun dispose() {
    runBlocking {
      InMemoryDataHandler.clearUsers()
    }
  }

  @Test
  fun `test user registration success`() = testApplication {
    val client = customSetupTestClient()

    val requestBody = CreateUserRequestDTO(
      name = "Test User",
      email = "test@example.com",
      plainPassword = "test123",
      timeIntervals = listOf(
        TimeInterval(
          enter = LocalTime(8, 0),
          exit = LocalTime(12, 0),
        )
      )
    )

    val response = client.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(requestBody)
    }

    assertEquals(expected = HttpStatusCode.Created, actual = response.status)
    val userId = response.bodyAsText().toIntOrNull()
    assertNotNull(userId, "User ID should be returned after registration")
  }

  @Test
  fun `test user registration with existing email fails`() = testApplication {
    val client = customSetupTestClient()

    val requestBody = CreateUserRequestDTO(
      name = "Test User",
      email = "test@example.com",
      plainPassword = "test123",
      timeIntervals = listOf(
        TimeInterval(
          enter = LocalTime(8, 0),
          exit = LocalTime(12, 0),
        )
      )
    )

    // First registration
    client.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(requestBody)
    }

    // Second registration with the same email
    val response = client.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(requestBody)
    }

    assertEquals(expected = HttpStatusCode.InternalServerError, actual = response.status)
//    assertEquals("Email already exists", response.bodyAsText())
  }

  @Test
  fun `test user login success`() = testApplication {
    val client = customSetupTestClient()

    val registrationRequest = CreateUserRequestDTO(
      name = "Test User",
      email = "test@example.com",
      plainPassword = "test123",
      timeIntervals = listOf(
        TimeInterval(
          enter = LocalTime(8, 0),
          exit = LocalTime(12, 0),
        )
      )
    )

    // Register the user
    client.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(registrationRequest)
    }

    val loginRequest = CredentialsDTO(
      email = "test@example.com",
      plainPassword = "test123"
    )

    val response = client.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(loginRequest)
    }

    assertEquals(expected = HttpStatusCode.OK, actual = response.status)
    val jwt = response.bodyAsText()
    assertTrue(jwt.isNotBlank(), "JWT should be returned on successful login")
  }

  @Test
  fun `test user login with incorrect password fails`() = testApplication {
    val client = customSetupTestClient()

    val registrationRequest = CreateUserRequestDTO(
      name = "Test User",
      email = "test@example.com",
      plainPassword = "test123",
      timeIntervals = listOf(
        TimeInterval(
          enter = LocalTime(8, 0),
          exit = LocalTime(12, 0),
        )
      )
    )

    // Register the user
    client.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(registrationRequest)
    }

    val loginRequest = CredentialsDTO(
      email = "test@example.com",
      plainPassword = "wrongpassword"
    )

    val response = client.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(loginRequest)
    }

    assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
  }

  @Test
  fun `test user login with non-existent email fails`() = testApplication {
    val client = customSetupTestClient()

    val loginRequest = CredentialsDTO(
      email = "nonexistent@example.com",
      plainPassword = "test123"
    )

    val response = client.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(loginRequest)
    }

    assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    assertEquals("Email not exists", response.bodyAsText())
  }
}