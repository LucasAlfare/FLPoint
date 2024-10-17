package routes

import USER_EMAIL
import USER_NAME
import USER_PASS
import com.lucasalfare.flpoint.server.CreateUserRequestDTO
import customSetupTestClient
import defaultUserTimeInterval
import defaultUserTimeZone
import disposeTestingDatabase
import initTestingDatabase
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PublicRoutesTests {

  @BeforeTest
  fun setup() {
    initTestingDatabase()
  }

  @AfterTest
  fun dispose() {
    disposeTestingDatabase()
  }

  @Test
  fun `test _register route success`() = testApplication {
    val c = customSetupTestClient()

    val registerResponse = c.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(
        CreateUserRequestDTO(
          name = USER_NAME,
          email = USER_EMAIL,
          plainPassword = USER_PASS,
          timeIntervals = listOf(defaultUserTimeInterval),
          timeZone = defaultUserTimeZone
        )
      )
    }

    assertEquals(expected = HttpStatusCode.Created, registerResponse.status)
  }

  @Test
  fun `test _register route failure`() = testApplication {
    val c = customSetupTestClient()

    val registerResponse = c.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(
        CreateUserRequestDTO(
          name = USER_NAME,
          email = USER_EMAIL,
          plainPassword = USER_PASS,
          timeIntervals = emptyList(),
          timeZone = defaultUserTimeZone
        )
      )
    }

    assertEquals(expected = HttpStatusCode.UnprocessableEntity, registerResponse.status)
  }

  @Test
  fun `test _register route duplicate user creation`() = testApplication {
    val c = customSetupTestClient()

    // first create one
    c.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(
        CreateUserRequestDTO(
          name = USER_NAME,
          email = USER_EMAIL,
          plainPassword = USER_PASS,
          timeIntervals = listOf(defaultUserTimeInterval),
          timeZone = defaultUserTimeZone
        )
      )
    }

    // try do the same after
    val registerResponse = c.post("/register") {
      contentType(ContentType.Application.Json)
      setBody(
        CreateUserRequestDTO(
          name = USER_NAME,
          email = USER_EMAIL,
          plainPassword = USER_PASS,
          timeIntervals = listOf(defaultUserTimeInterval),
          timeZone = defaultUserTimeZone
        )
      )
    }

    assertEquals(expected = HttpStatusCode.InternalServerError, registerResponse.status)
  }
}