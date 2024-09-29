import com.lucasalfare.flpoint.server.CreateUserRequestDTO
import com.lucasalfare.flpoint.server.CredentialsDTO
import com.lucasalfare.flpoint.server.InMemoryDataHandler
import com.lucasalfare.flpoint.server.TimeInterval
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PointTests {

  @AfterTest
  fun dispose() {
    runBlocking {
      InMemoryDataHandler.clearUsers()
    }
  }

  @Test
  fun `test create point with success authentication`() = testApplication {
    val client = customSetupTestClient()
    client.post("/register") {
      contentType(ContentType.Application.Json)
      val now = Clock.System.now()
      setBody(
        CreateUserRequestDTO(
          name = "Test User",
          email = "example@test.com",
          plainPassword = "test123",
          timeIntervals = listOf(
            TimeInterval(
              enter = LocalTime(
                hour = (now.toLocalDateTime(TimeZone.UTC).hour - 2) % 24,
                minute = 0
              ),
              exit = LocalTime(
                hour = (now.toLocalDateTime(TimeZone.UTC).hour + 2) % 24,
                minute = 0
              )
            )
          )
        )
      )
    }

    val loginResponse = client.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(CredentialsDTO(email = "example@test.com", plainPassword = "test123"))
    }

    val jwt = loginResponse.bodyAsText()
    val createPointResponse = client.post("/point") {
      bearerAuth(jwt)
    }

    assertEquals(
      expected = HttpStatusCode.Created,
      actual = createPointResponse.status
    )
  }

  @Test
  fun `test create point with failed authentication`() = testApplication {
    val client = customSetupTestClient()
    client.post("/register") {
      contentType(ContentType.Application.Json)
      val now = Clock.System.now()
      setBody(
        CreateUserRequestDTO(
          name = "Test User",
          email = "example@test.com",
          plainPassword = "test123",
          timeIntervals = listOf(
            TimeInterval(
              enter = LocalTime(
                hour = (now.toLocalDateTime(TimeZone.UTC).hour - 2) % 24,
                minute = 0
              ),
              exit = LocalTime(
                hour = (now.toLocalDateTime(TimeZone.UTC).hour + 2) % 24,
                minute = 0
              )
            )
          )
        )
      )
    }

    val loginResponse = client.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(CredentialsDTO(email = "example@test.com", plainPassword = "WRONG!!"))
    }

    val jwt = loginResponse.bodyAsText()
    val createPointResponse = client.post("/point") { bearerAuth(jwt) }

    assertEquals(
      expected = HttpStatusCode.BadRequest,
      actual = createPointResponse.status
    )
  }
}