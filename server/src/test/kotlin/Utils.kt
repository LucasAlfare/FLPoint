import com.lucasalfare.flpoint.server.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.hours

internal const val USER_ADMIN_NAME = "Admin Master"
internal const val USER_ADMIN_EMAIL = "admin@system.com"
internal const val USER_ADMIN_PASS = "admin_password"

internal const val USER_NAME = "User Common"
internal const val USER_EMAIL = "user@common.com"
internal const val USER_PASS = "user12345"

internal val defaultUserTimeZone = TimeZone.of("America/Sao_Paulo")

/*
TODO: solve this:
We have a problem.
If we run this code, eg, at "22h" (night) we end with intervals like

enter=20h and exit=00h

knowing that, exit will always be "earlier"
*/
internal fun getDefaultUserTimeInterval(): TimeInterval {
  val now = Clock.System.now()
  val nextEnter = (now - 2.hours).toLocalDateTime(defaultUserTimeZone)
  val nextExit = (now + 2.hours).toLocalDateTime(defaultUserTimeZone)
  return TimeInterval(
    enter = nextEnter.time,
    exit = nextExit.time
  )
}

internal fun ApplicationTestBuilder.customSetupTestClient(): HttpClient {
  application {
    initKtorConfiguration()
  }

  return createClient {
    install(ContentNegotiation) {
      json(Json { isLenient = false })
    }
  }
}

internal fun initTestingDatabase() {
  if (!AppDB.isDatabaseConnected()) {
    AppDB.initialize(
      jdbcUrl = Constants.DATABASE_H2_URL,
      jdbcDriverClassName = Constants.DATABASE_H2_DRIVER,
      username = "",
      password = "",
      maximumPoolSize = 5
    )
  }

  transaction {
    SchemaUtils.createMissingTablesAndColumns(
      Users, Points
    )
  }
}

internal fun disposeTestingDatabase() {
  transaction {
    SchemaUtils.drop(Users, Points)
  }
}

internal fun getSomeAdmin() = User(
  id = 1,
  name = USER_ADMIN_NAME,
  email = USER_ADMIN_EMAIL,
  hashedPassword = USER_ADMIN_PASS,
  isAdmin = true,
  timeIntervals = emptyList(),
  timeZone = TimeZone.of("America/Sao_Paulo")
)

internal fun getSomeUser() = User(
  id = 1,
  name = USER_NAME,
  email = USER_EMAIL,
  hashedPassword = USER_PASS,
  isAdmin = false,
  timeIntervals = listOf(getDefaultUserTimeInterval()),
  timeZone = TimeZone.of("America/Sao_Paulo")
)

internal suspend fun signupUserForTest(
  client: HttpClient,
  createUserRequestDTO: CreateUserRequestDTO
) = client.post("/register") {
  contentType(ContentType.Application.Json)
  setBody(createUserRequestDTO)
}

internal suspend fun loginUserForTest(client: HttpClient, credentialsDTO: CredentialsDTO) = client.post("/login") {
  contentType(ContentType.Application.Json)
  setBody(credentialsDTO)
}