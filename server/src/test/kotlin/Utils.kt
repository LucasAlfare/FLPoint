import com.lucasalfare.flpoint.server.config.initKtorConfiguration
import com.lucasalfare.flpoint.server.infrastructure.persistence.AppDB
import com.lucasalfare.flpoint.server.infrastructure.persistence.Users
import com.lucasalfare.flpoint.server.infrastructure.persistence.Points
import com.lucasalfare.flpoint.server.infrastructure.persistence.SchemaUtils
import com.lucasalfare.flpoint.server.shared.Constants
import com.lucasalfare.flpoint.server.domain.model.User
import com.lucasalfare.flpoint.server.domain.model.CreateUserRequestDTO
import com.lucasalfare.flpoint.server.domain.model.CredentialsDTO
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

internal const val USER_ADMIN_NAME = "Admin Master"
internal const val USER_ADMIN_EMAIL = "admin@system.com"
internal const val USER_ADMIN_PASS = "admin_password"

internal const val USER_NAME = "User Common"
internal const val USER_EMAIL = "user@common.com"
internal const val USER_PASS = "user12345"

internal val defaultUserTimeZone = TimeZone.of("America/Sao_Paulo")

// Note: TimeInterval functionality removed from domain model
// This function is kept for compatibility but may need adjustment
internal fun getDefaultUserTimeZone(): TimeZone {
  return defaultUserTimeZone
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
    SchemaUtils.create(
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
  timeZone = TimeZone.of("America/Sao_Paulo"),
  isAdmin = true
)

internal fun getSomeUser() = User(
  id = 1,
  name = USER_NAME,
  email = USER_EMAIL,
  hashedPassword = USER_PASS,
  timeZone = TimeZone.of("America/Sao_Paulo"),
  isAdmin = false
)

internal suspend fun signupUserForTest(
  client: HttpClient,
  createUserRequestDTO: CreateUserRequestDTO
) = client.post("/admin/register") {
  contentType(ContentType.Application.Json)
  setBody(createUserRequestDTO)
}

internal suspend fun loginUserForTest(client: HttpClient, credentialsDTO: CredentialsDTO) = client.post("/login") {
  contentType(ContentType.Application.Json)
  setBody(credentialsDTO)
}