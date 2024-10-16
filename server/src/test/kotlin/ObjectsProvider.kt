import com.lucasalfare.flpoint.server.TimeInterval
import com.lucasalfare.flpoint.server.User
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

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
