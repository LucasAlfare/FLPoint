package com.lucasalfare.flpoint.server.test.routes

import com.lucasalfare.flpoint.server.model.Credentials
import com.lucasalfare.flpoint.server.model.SystemRules
import com.lucasalfare.flpoint.server.model.User
import com.lucasalfare.flpoint.server.service.MongoUsersService
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class UserRouteTest {

  @BeforeTest
  fun setup() {
    clearUsersCollection()
  }

  @Test
  fun `test create user`() = testApplication {
    val cli = createClient {
      install(ContentNegotiation) {
        json()
      }
    }

    val nextUser = User(credentials = Credentials("lucas", "123"))

    val postResponse = cli.post("/api/users") {
      contentType(ContentType.Application.Json)
      setBody(nextUser)
    }

    assertEquals(HttpStatusCode.OK, postResponse.status)
  }

  @Test
  fun `test get user`() = testApplication {
    val cli = createClient {
      install(ContentNegotiation) {
        json()
      }
    }

    val nextUser = User(credentials = Credentials("lucas", "123"))

    val postResponse = cli.post("/api/users") {
      contentType(ContentType.Application.Json)
      setBody(nextUser)
    }
    val postUserCreated = postResponse.body<User>()

    val getUserResponse = cli.get("/api/users/${postUserCreated.id}")
    assertEquals(HttpStatusCode.OK, getUserResponse.status)
  }

  @Test
  fun `test get all`() = testApplication {
    val cli = createClient {
      install(ContentNegotiation) {
        json()
      }
    }

    cli.post("/api/users") {
      contentType(ContentType.Application.Json)
      setBody(
        User(
          credentials = Credentials(
            "lucas", "123"
          )
        )
      )
    }

    val getAllResponse = cli.get("/api/users")
    assertEquals(HttpStatusCode.OK, getAllResponse.status)

    val allUsers = getAllResponse.body<List<User>>()
    assertTrue { allUsers.isNotEmpty() }
    assertTrue { allUsers.size == 1 }
  }

  @Test
  fun `test update user`() = testApplication {
    val cli = createClient {
      install(ContentNegotiation) {
        json()
      }
    }
    val nextUser = User(
      maxAuthenticationsPerDay = SystemRules.DEFAULT_MAX_POINT_REGISTRATIONS_PER_DAY,
      credentials = Credentials("lucas", "123")
    )
    val postCreateResponse = cli.post("/api/users") {
      contentType(ContentType.Application.Json)
      setBody(nextUser)
    }
    val responseUser = postCreateResponse.body<User>()

    val nextNumberValue = 99999
    val patchUpdateResponse = cli.patch("/api/users/${responseUser.id}") {
      contentType(ContentType.Application.Json)
      setBody(
        User(maxAuthenticationsPerDay = nextNumberValue)
      )
    }

    assertEquals(HttpStatusCode.OK, patchUpdateResponse.status)

    val getUserResponse = cli.get("/api/users/${responseUser.id}")
    assertEquals(HttpStatusCode.OK, getUserResponse.status)

    val finalUser = getUserResponse.body<User>()
    assertEquals(nextNumberValue, finalUser.maxAuthenticationsPerDay)
  }

  @Test
  fun `test remove user`() = testApplication {
    val cli = createClient {
      install(ContentNegotiation) {
        json()
      }
    }
    val nextUser = User(credentials = Credentials("lucas", "123"))
    val postResponse = cli.post("/api/users") {
      contentType(ContentType.Application.Json)
      setBody(nextUser)
    }
    assertEquals(HttpStatusCode.OK, postResponse.status)

    val deleteResponse = cli.delete("/api/users/${nextUser.id}")
    assertEquals(HttpStatusCode.OK, deleteResponse.status)

    val getAllResponse = cli.get("/api/users")
    assertEquals(HttpStatusCode.OK, getAllResponse.status)
    val allUsers = getAllResponse.body<List<User>>()
    assertTrue { allUsers.isEmpty() }
  }

  @Test
  fun `test clear`() = testApplication {
    val cli = createClient {
      install(ContentNegotiation) {
        json()
      }
    }

    cli.post("/api/users") {
      contentType(ContentType.Application.Json)
      setBody(
        User(
          credentials = Credentials(
            "lucas", "123"
          )
        )
      )
    }

    val deleteResponse = cli.delete("/api/users")
    assertEquals(HttpStatusCode.OK, deleteResponse.status)

    val getAllResponse = cli.get("/api/users")
    assertEquals(HttpStatusCode.OK, getAllResponse.status)

    val allUsers = getAllResponse.body<List<User>>()
    assertTrue { allUsers.isEmpty() }
  }

  @AfterTest
  fun dispose() {
    clearUsersCollection()
  }

  private fun clearUsersCollection() {
    runBlocking {
      // manually clearing the remote
      // database before each test
      // This function is pretty tested by itself
      MongoUsersService.clear()
    }
  }
}