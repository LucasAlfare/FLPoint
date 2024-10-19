package routes

import com.lucasalfare.flpoint.server.*
import customSetupTestClient
import disposeTestingDatabase
import getSomeUser
import initTestingDatabase
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.datetime.LocalTime
import loginUserForTest
import signupUserForTest
import kotlin.test.*

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
  fun `test PATCH _users_update-password route success`() = testApplication {
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
  fun `test PATCH _users_update-password route failure`() = testApplication {
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

  @Test
  fun `test POST _users_points route success`() = testApplication {
    val c = customSetupTestClient()
    val nextBasicUser = getSomeUser()

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

    val loginResponse = loginUserForTest(
      client = c,
      credentialsDTO = CredentialsDTO(nextBasicUser.email, nextBasicUser.hashedPassword)
    )

    val receivedJwt = loginResponse.bodyAsText()
    val createPointResponse = c.post("/users/point") {
      bearerAuth(receivedJwt)
    }

    assertEquals(expected = HttpStatusCode.Created, actual = createPointResponse.status)
  }

  @Test
  fun `test POST _users_points route out of time interval failure`() = testApplication {
    val c = customSetupTestClient()
    val nextBasicUser = getSomeUser()

    // we prepare testing user with a different time interval
    // TODO: we need change this values to any time different of the time that the test is running!
    nextBasicUser.timeIntervals = listOf(
      TimeInterval(
        enter = LocalTime(hour = 8, minute = 0),
        exit = LocalTime(hour = 12, minute = 0)
      )
    )

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

    val loginResponse = loginUserForTest(
      client = c,
      credentialsDTO = CredentialsDTO(nextBasicUser.email, nextBasicUser.hashedPassword)
    )

    val receivedJwt = loginResponse.bodyAsText()
    val createPointResponse = c.post("/users/point") {
      bearerAuth(receivedJwt)
    }

    assertEquals(expected = HttpStatusCode.UnprocessableEntity, actual = createPointResponse.status)
  }

  @Test
  fun `test POST _users_points route too many point creation failure`() = testApplication {
    val c = customSetupTestClient()
    val nextBasicUser = getSomeUser()

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

    val loginResponse = loginUserForTest(
      client = c,
      credentialsDTO = CredentialsDTO(nextBasicUser.email, nextBasicUser.hashedPassword)
    )

    val receivedJwt = loginResponse.bodyAsText()

    // we try to create a point
    c.post("/users/point") {
      bearerAuth(receivedJwt)
    }

    // after we try to create other!
    val createPointResponse = c.post("/users/point") {
      bearerAuth(receivedJwt)
    }

    assertEquals(expected = HttpStatusCode.UnprocessableEntity, actual = createPointResponse.status)
  }

  @Test
  fun `test GET _users_points route success`() = testApplication {
    val c = customSetupTestClient()
    val nextBasicUser = getSomeUser()

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

    val loginResponse = loginUserForTest(
      client = c,
      credentialsDTO = CredentialsDTO(nextBasicUser.email, nextBasicUser.hashedPassword)
    )

    val receivedJwt = loginResponse.bodyAsText()

    c.post("/users/point") {
      bearerAuth(receivedJwt)
    }

    val getOwnPointsResponse = c.get("/users/points") {
      bearerAuth(receivedJwt)
    }

    assertEquals(expected = HttpStatusCode.OK, actual = getOwnPointsResponse.status)
    assertTrue(getOwnPointsResponse.body<List<PointDTO>>().isNotEmpty())
  }
}