package routes

import com.lucasalfare.flpoint.server.AppUsecases
import com.lucasalfare.flpoint.server.CreateUserRequestDTO
import com.lucasalfare.flpoint.server.CredentialsDTO
import customSetupTestClient
import disposeTestingDatabase
import getSomeAdmin
import initTestingDatabase
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AdminOnlyRoutesTests {

  private val admin = getSomeAdmin()

  @BeforeTest
  fun init() {
    initTestingDatabase()

    // we always create an admin user in the testing database
    runBlocking {
      AppUsecases.signupUser(
        createUserRequestDTO = CreateUserRequestDTO(
          name = admin.name,
          email = admin.email,
          plainPassword = admin.hashedPassword,
          timeIntervals = admin.timeIntervals,
          timeZone = admin.timeZone
        ),
        isAdmin = true
      )
    }
  }

  @AfterTest
  fun dispose() {
    disposeTestingDatabase()
  }

  @Test
  fun `test GET _admin_users success`() = testApplication {
    val c = customSetupTestClient()

    val loginResponse = c.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(CredentialsDTO(email = admin.email, plainPassword = admin.hashedPassword))
    }

    println("login response: [${loginResponse.bodyAsText()}]")

    val adminJwt = loginResponse.bodyAsText()

    val getAllUsersResponse = c.get("/admin/users") {
      bearerAuth(adminJwt)
    }

    println("getAllUsersResponse: [${getAllUsersResponse.bodyAsText()}]")

    assertEquals(expected = HttpStatusCode.OK, actual = getAllUsersResponse.status)
  }
}