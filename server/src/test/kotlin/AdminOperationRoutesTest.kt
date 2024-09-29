import com.lucasalfare.flpoint.server.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.*

class AdminOperationRoutesTest {

  @BeforeTest
  fun setup() {
    runBlocking {
      InMemoryDataHandler.signupUser(
        dto = CreateUserRequestDTO(
          name = "Test User",
          email = "admin@example.abc",
          plainPassword = "test123",
          timeIntervals = listOf(
            TimeInterval(
              enter = LocalTime(8, 0),
              exit = LocalTime(12, 0),
            )
          )
        ),
        isAdmin = true
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
  fun `test admin get all users success`() = testApplication {
    val client = customSetupTestClient()

    client.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(
        CreateUserRequestDTO(
          name = "Test User 2",
          email = "user@example.abc",
          plainPassword = "test123",
          timeIntervals = listOf(
            TimeInterval(
              enter = LocalTime(
                hour = 8,
                minute = 0
              ),
              exit = LocalTime(
                hour = 12,
                minute = 0
              )
            )
          )
        )
      )
    }

    val adminJwt = client.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(CredentialsDTO("admin@example.abc", "test123"))
    }.bodyAsText()

    val allUsersResponse = client.get("/admin/users") {
      bearerAuth(adminJwt)
    }

    assertEquals(expected = HttpStatusCode.OK, actual = allUsersResponse.status)
    assertTrue(allUsersResponse.body<List<GetUserResponseDTO>>().size == 2, "received list must be not empty")
  }

  @Test
  fun `test admin get all users failure`() = testApplication {
    val client = customSetupTestClient()

    val adminJwt = client.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(CredentialsDTO("admin@example.abc", "test123"))
    }.bodyAsText()

    val allUsersResponse = client.get("/admin/users") {
      bearerAuth(adminJwt)
    }

    println(allUsersResponse.bodyAsText())

    assertEquals(expected = HttpStatusCode.OK, actual = allUsersResponse.status)
    assertTrue(allUsersResponse.body<List<GetUserResponseDTO>>().size == 1, "received list must be empty")
  }

  @Test
  fun `test admin get all points success`() = testApplication {
    val client = customSetupTestClient()

    client.post("/register") {
      contentType(ContentType.Application.Json)
      val now = Clock.System.now()
      setBody(
        CreateUserRequestDTO(
          name = "Test User 2",
          email = "user@example.abc",
          plainPassword = "test123",
          timeIntervals = listOf(
            TimeInterval(
              enter = LocalTime(
                hour = (now.toLocalDateTime(TimeZone.UTC).hour - 2) % 23,
                minute = 0
              ),
              exit = LocalTime(
                hour = (now.toLocalDateTime(TimeZone.UTC).hour + 2) % 23,
                minute = 0
              )
            )
          )
        )
      )
    }

    val userJwt = client.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(CredentialsDTO("user@example.abc", "test123"))
    }.bodyAsText()

    val createPointResponse = client.post("/point") { bearerAuth(userJwt) }
    println(">>> createPointResponse.status=[${createPointResponse.status}]")

    val adminJwt = client.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(CredentialsDTO("admin@example.abc", "test123"))
    }.bodyAsText()

    val allPointsResponse = client.get("/admin/points") { bearerAuth(adminJwt) }

    assertEquals(expected = HttpStatusCode.OK, actual = allPointsResponse.status)
    assertTrue(allPointsResponse.body<List<GetPointRequestDTO>>().size == 1, "received list must be not empty")
  }
}