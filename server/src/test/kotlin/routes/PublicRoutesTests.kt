package routes

import USER_EMAIL
import USER_NAME
import USER_PASS
import com.lucasalfare.flpoint.server.CreateUserRequestDTO
import com.lucasalfare.flpoint.server.CredentialsDTO
import customSetupTestClient
import defaultUserTimeZone
import disposeTestingDatabase
import getDefaultUserTimeInterval
import initTestingDatabase
import io.ktor.http.*
import io.ktor.server.testing.*
import loginUserForTest
import signupUserForTest
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
    val registerResponse = signupUserForTest(
      client = c,
      createUserRequestDTO = CreateUserRequestDTO(
        name = USER_NAME,
        email = USER_EMAIL,
        plainPassword = USER_PASS,
        timeIntervals = listOf(getDefaultUserTimeInterval()),
        timeZone = defaultUserTimeZone
      )
    )
    assertEquals(expected = HttpStatusCode.Created, registerResponse.status)
  }

  @Test
  fun `test _register route failure`() = testApplication {
    val c = customSetupTestClient()

    val registerResponse = signupUserForTest(
      client = c,
      createUserRequestDTO = CreateUserRequestDTO(
        name = USER_NAME,
        email = USER_EMAIL,
        plainPassword = USER_PASS,
        timeIntervals = emptyList(),
        timeZone = defaultUserTimeZone
      )
    )

    assertEquals(expected = HttpStatusCode.UnprocessableEntity, registerResponse.status)
  }

  @Test
  fun `test _register route duplicate user creation`() = testApplication {
    val c = customSetupTestClient()

    // first create one
    signupUserForTest(
      client = c,
      createUserRequestDTO = CreateUserRequestDTO(
        name = USER_NAME,
        email = USER_EMAIL,
        plainPassword = USER_PASS,
        timeIntervals = listOf(getDefaultUserTimeInterval()),
        timeZone = defaultUserTimeZone
      )
    )

    // try do the same after
    val registerResponse = signupUserForTest(
      client = c,
      createUserRequestDTO = CreateUserRequestDTO(
        name = USER_NAME,
        email = USER_EMAIL,
        plainPassword = USER_PASS,
        timeIntervals = listOf(getDefaultUserTimeInterval()),
        timeZone = defaultUserTimeZone
      )
    )

    assertEquals(expected = HttpStatusCode.InternalServerError, registerResponse.status)
  }

  @Test
  fun `test _long route success`() = testApplication {
    val c = customSetupTestClient()

    // first create a user (success)
    signupUserForTest(
      client = c,
      createUserRequestDTO = CreateUserRequestDTO(
        name = USER_NAME,
        email = USER_EMAIL,
        plainPassword = USER_PASS,
        timeIntervals = listOf(getDefaultUserTimeInterval()),
        timeZone = defaultUserTimeZone
      )
    )

    // now tries to send credentials to /login route
    val loginResponse = loginUserForTest(
      client = c,
      credentialsDTO = CredentialsDTO(email = USER_EMAIL, plainPassword = USER_PASS)
    )

    assertEquals(expected = HttpStatusCode.OK, loginResponse.status)
  }

  @Test
  fun `test _long route failure`() = testApplication {
    val c = customSetupTestClient()

    // first create a user (success)
    signupUserForTest(
      client = c,
      createUserRequestDTO = CreateUserRequestDTO(
        name = USER_NAME,
        email = USER_EMAIL,
        plainPassword = USER_PASS,
        timeIntervals = listOf(getDefaultUserTimeInterval()),
        timeZone = defaultUserTimeZone
      )
    )

    // now tries to send BAD credentials to /login route
    val loginResponse = loginUserForTest(
      client = c,
      credentialsDTO = CredentialsDTO(email = USER_EMAIL, plainPassword = "BAD pass")
    )

    assertEquals(expected = HttpStatusCode.Unauthorized, actual = loginResponse.status)
  }
}