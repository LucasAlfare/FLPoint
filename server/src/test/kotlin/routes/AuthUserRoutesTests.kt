package routes

import com.lucasalfare.flpoint.server.CreateUserRequestDTO
import com.lucasalfare.flpoint.server.CredentialsDTO
import com.lucasalfare.flpoint.server.UpdateUserPasswordRequestDTO
import customSetupTestClient
import disposeTestingDatabase
import getSomeUser
import initTestingDatabase
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import loginUserForTest
import signupUserForTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthUserRoutesTests {

  @BeforeTest
  fun init() {
    initTestingDatabase()
  }

  @AfterTest
  fun dispose() {
    disposeTestingDatabase()
  }

  @Test
  fun `test _users_update-password route success`() = testApplication {
    val c = customSetupTestClient()

    val nextBasicUser = getSomeUser()

    // first create an user
    signupUserForTest(
      client = c,
      createUserRequestDTO = CreateUserRequestDTO(
        name = nextBasicUser.name,
        email = nextBasicUser.email,
        plainPassword = nextBasicUser.hashedPassword,
        timeIntervals = nextBasicUser.timeIntervals,
        timeZone = nextBasicUser.timeZone,
      )
    )

    // then log in him
    val loginResponse = loginUserForTest(
      client = c,
      credentialsDTO = CredentialsDTO(nextBasicUser.email, nextBasicUser.hashedPassword)
    )

    val receivedJwt = loginResponse.bodyAsText()

    val updatePasswordResponse = c.patch("/users/update-password") {
      contentType(ContentType.Application.Json)
      bearerAuth(receivedJwt)
      setBody(
        UpdateUserPasswordRequestDTO(
          currentPlainPassword = nextBasicUser.hashedPassword, newPlainPassword = "hehehe"
        )
      )
    }

    assertEquals(expected = HttpStatusCode.OK, actual = updatePasswordResponse.status)
  }

  @Test
  fun `test _users_update-password route failure`() = testApplication {
    val c = customSetupTestClient()

    val nextBasicUser = getSomeUser()

    // first create an user
    signupUserForTest(
      client = c,
      createUserRequestDTO = CreateUserRequestDTO(
        name = nextBasicUser.name,
        email = nextBasicUser.email,
        plainPassword = nextBasicUser.hashedPassword,
        timeIntervals = nextBasicUser.timeIntervals,
        timeZone = nextBasicUser.timeZone,
      )
    )

    // then log in him
    val loginResponse = loginUserForTest(
      client = c,
      credentialsDTO = CredentialsDTO(nextBasicUser.email, nextBasicUser.hashedPassword)
    )

    val receivedJwt = loginResponse.bodyAsText()

    // we send a BAD current plain password
    val updatePasswordResponse = c.patch("/users/update-password") {
      contentType(ContentType.Application.Json)
      bearerAuth(receivedJwt)
      setBody(
        UpdateUserPasswordRequestDTO(
          currentPlainPassword = "BAD CURRENT PASS!", newPlainPassword = "hehehe"
        )
      )
    }

    assertEquals(expected = HttpStatusCode.Unauthorized, actual = updatePasswordResponse.status)
  }
}