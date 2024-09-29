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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AdminOnlyRouteTest {

  @BeforeTest
  fun setup() {
    runBlocking {
      InMemoryDataHandler.signupUser(
        CreateUserRequestDTO(
          name = "Test User",
          email = "admin@example.com",
          plainPassword = "test123",
          timeIntervals = listOf(
            TimeInterval(
              enter = LocalTime(8, 0),
              exit = LocalTime(12, 0),
            )
          )
        ),
        true
      )

      InMemoryDataHandler.signupUser(
        CreateUserRequestDTO(
          name = "Test User",
          email = "test@example.com",
          plainPassword = "test123",
          timeIntervals = listOf(
            TimeInterval(
              enter = LocalTime(8, 0),
              exit = LocalTime(12, 0),
            )
          )
        ),
        false
      )
    }
  }

  @AfterTest
  fun dispose() {
    runBlocking {
      InMemoryDataHandler.clearUsers()
    }
  }

  @Test
  fun `test authorized admin success`() = testApplication {
    val client = customSetupTestClient()

    val loginRequest = CredentialsDTO(
      email = "admin@example.com",
      plainPassword = "test123"
    )

    val jwt = client.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(loginRequest)
    }.bodyAsText()

    val adminOnlyRouteResponse = client.get("/admin-only") {
      header(HttpHeaders.Authorization, "Bearer $jwt")
    }

    assertEquals(expected = HttpStatusCode.OK, actual = adminOnlyRouteResponse.status)
  }

  @Test
  fun `test unauthorized admin failure`() = testApplication {
    val client = customSetupTestClient()

    val loginRequest = CredentialsDTO(
      email = "test@example.com",
      plainPassword = "test123"
    )

    val jwt = client.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(loginRequest)
    }.bodyAsText()

    val adminOnlyRouteResponse = client.get("/admin-only") {
      header(HttpHeaders.Authorization, "Bearer $jwt")
    }

    assertEquals(expected = HttpStatusCode.Forbidden, actual = adminOnlyRouteResponse.status)
  }
}