import com.lucasalfare.flpoint.server.*
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

internal const val USER_ADMIN_NAME = "Admin Master"
internal const val USER_ADMIN_EMAIL = "admin@system.com"
internal const val USER_ADMIN_PASS = "admin_password"

internal const val USER_NAME = "User Common"
internal const val USER_EMAIL = "user@common.com"
internal const val USER_PASS = "user12345"

internal val defaultUserTimeInterval = TimeInterval(
  enter = LocalTime(hour = 8, minute = 0),
  exit = LocalTime(hour = 12, minute = 0)
)

internal val defaultUserTimeZone = TimeZone.of("America/Sao_Paulo")

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

fun getSomeAdmin() = User(
  id = 1,
  name = USER_ADMIN_NAME,
  email = USER_ADMIN_EMAIL,
  hashedPassword = USER_ADMIN_PASS,
  isAdmin = true,
  timeIntervals = emptyList(),
  timeZone = TimeZone.of("America/Sao_Paulo")
)

fun getSomeUser() = User(
  id = 1,
  name = USER_NAME,
  email = USER_EMAIL,
  hashedPassword = USER_PASS,
  isAdmin = false,
  timeIntervals = listOf(defaultUserTimeInterval),
  timeZone = TimeZone.of("America/Sao_Paulo")
)