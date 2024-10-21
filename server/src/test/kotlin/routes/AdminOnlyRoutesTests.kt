package routes

import USER_EMAIL
import USER_NAME
import USER_PASS
import com.lucasalfare.flpoint.server.AppUsecases
import com.lucasalfare.flpoint.server.CreateUserRequestDTO
import com.lucasalfare.flpoint.server.CredentialsDTO
import customSetupTestClient
import defaultUserTimeZone
import disposeTestingDatabase
import getDefaultUserTimeInterval
import getSomeAdmin
import initTestingDatabase
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import signupUserForTest
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

    val adminJwt = loginResponse.bodyAsText()

    val getAllUsersResponse = c.get("/admin/users") {
      bearerAuth(adminJwt)
    }

    assertEquals(expected = HttpStatusCode.OK, actual = getAllUsersResponse.status)
  }

  @Test
  fun `test PATCH _admin_users_{id}_update-time-intervals success`() = testApplication {
    val c = customSetupTestClient()

    // we prepare the dummy user and store its ID
    val receivedUserId = signupUserForTest(
      client = c,
      createUserRequestDTO = CreateUserRequestDTO(
        name = USER_NAME,
        email = USER_EMAIL,
        plainPassword = USER_PASS,
        timeIntervals = listOf(getDefaultUserTimeInterval()),
        timeZone = defaultUserTimeZone
      )
    ).body<Int>()

    // we log in the admin
    val loginResponse = c.post("/login") {
      contentType(ContentType.Application.Json)
      setBody(CredentialsDTO(email = admin.email, plainPassword = admin.hashedPassword))
    }

    val adminJwt = loginResponse.bodyAsText()

    // we create a "new" time intervals for the existing user ID
    // for instance, just some repeated values
    val nextTimeIntervals =
      listOf(getDefaultUserTimeInterval(), listOf(getDefaultUserTimeInterval()), listOf(getDefaultUserTimeInterval()))

    val updateTimeIntervalsResponse = c.patch("/admin/users/$receivedUserId/update-time-intervals") {
      bearerAuth(adminJwt)
      setBody(nextTimeIntervals)
    }

    assertEquals(expected = HttpStatusCode.OK, actual = updateTimeIntervalsResponse.status)
  }
}