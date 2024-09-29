@file:Suppress("unused")

package com.lucasalfare.flpoint.server

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.mindrot.jbcrypt.BCrypt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

//<editor-fold desc="EXTENSIONS-SECTION">
fun LocalTime.asInstant(
  today: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
): Instant = LocalDateTime(
  year = today.year,
  monthNumber = today.monthNumber,
  dayOfMonth = today.dayOfMonth,
  hour = hour,
  minute = minute,
  second = second
).toInstant(timeZone = TimeZone.UTC)

fun Throwable.customRootCause(): Throwable {
  var current = this
  while (true) {
    if (current.cause == null) return current
    current = current.cause!!
  }
}
//</editor-fold>

//<editor-fold desc="MODELING-SECTION">
class Constants {
  companion object {
    const val DEFAULT_MIN_PASSWORD_LENGTH = 4

    const val DEFAULT_LOWER_POINT_TOLERANCE_DURATION = 5 // minutes
    const val DEFAULT_HIGHER_POINT_TOLERANCE_DURATION = 5 // minutes
    const val DEFAULT_JWT_EXPIRATION_TIME = 10 // minutes
  }
}

open class AppError(description: String) : Throwable(description)
class DataHandlingError(message: String = "DataHandlingError") : AppError(message)
class AuthenticationError(message: String = "AuthenticationError") : AppError(message)
class RuleViolatedError(message: String = "RulesNotMatched") : AppError(message)
class NoPrivilegeError(message: String = "NoPrivilegeError") : AppError(message)
// ...

fun validateName(name: String) {
  if (name.isBlank()) throw DataHandlingError("Name cannot be empty")
  if (name.length < 2) throw DataHandlingError("Name must have at least 2 characters")
}

fun validateEmail(email: String) {
  val emailRegex = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$".toRegex()
  if (!email.matches(emailRegex)) throw DataHandlingError("Invalid email format")
}

fun validatePassword(password: String) {
  if (password.length < Constants.DEFAULT_MIN_PASSWORD_LENGTH)
    throw DataHandlingError("Password must have at least ${Constants.DEFAULT_MIN_PASSWORD_LENGTH} characters")
}

@Serializable
data class TimeInterval(
  val enter: LocalTime,
  val exit: LocalTime
)

// TODO: in the future include info about TIME_ZONE!
// TODO: we must be able to validate times using the stored user TZ!
data class User(
  val id: Int,
  val name: String,
  val email: String,
  val hashedPassword: String,
  val timeIntervals: List<TimeInterval>,
  val isAdmin: Boolean
)

data class Point(
  val id: Int,
  val relatedUserId: Int,
  val instant: Instant
)

@Serializable
data class CreateUserRequestDTO(
  val name: String,
  val email: String,
  val plainPassword: String,
  val timeIntervals: List<TimeInterval>
) {
  init {
    validateName(name)
    validateEmail(email)
    validatePassword(plainPassword)

    if (timeIntervals.isEmpty())
      throw RuleViolatedError("At least one time interval must be provided")
  }
}

@Serializable
data class CreatePointRequestDTO(
  val relatedUserId: Int
) {
  init {
    if (relatedUserId <= 0)
      throw DataHandlingError("Invalid user ID")
  }
}

@Serializable
data class CredentialsDTO(
  val email: String,
  val plainPassword: String
) {
  init {
    validateEmail(email)
    validatePassword(plainPassword)
  }
}

@Serializable
data class GetUserResponseDTO(
  val id: Int,
  val name: String,
  val email: String,
  val timeIntervals: List<TimeInterval>,
  val isAdmin: Boolean
)

@Serializable
data class GetPointRequestDTO(
  val id: Int,
  val relatedUserId: Int,
  val instant: Instant
)

interface DataHandler {

  // returns user ID
  suspend fun signupUser(dto: CreateUserRequestDTO, isAdmin: Boolean = false): Int

  // returns JWT
  suspend fun loginUser(dto: CredentialsDTO): String

  // Assumes is authenticated. Returns ID.
  suspend fun createPointFor(userId: Int): Int

  // Assumes is authenticated
  suspend fun getAllUserPoints(userId: Int): List<GetPointRequestDTO>

  // Assumes is authenticated. Admin only
  suspend fun getAllUsers(): List<GetUserResponseDTO>

  // Assumes is authenticated. Admin only
  suspend fun getAllPoints(): List<GetPointRequestDTO>

  // Assumes is authenticated. Admin only
  suspend fun deleteUser(userId: Int)

  // Assumes is authenticated. Admin only
  suspend fun deletePoint(pointId: Int)

  // Assumes is authenticated. Admin only
  suspend fun clearPoints()

  // Assumes is authenticated. Admin only
  suspend fun clearUsers()
}
//</editor-fold>

//<editor-fold desc="RULES-SECTION">
fun instantIsInValidTimeInterval(
  check: Instant,
  timeIntervals: List<TimeInterval>,
  lowerTolerance: Duration = Constants.DEFAULT_LOWER_POINT_TOLERANCE_DURATION.minutes,
  higherTolerance: Duration = Constants.DEFAULT_HIGHER_POINT_TOLERANCE_DURATION.minutes
): Boolean {
  for (interval in timeIntervals) {
    val nextEnter = interval.enter.asInstant() - lowerTolerance
    val nextExit = interval.exit.asInstant() + higherTolerance
    if (check in nextEnter..nextExit) return true // if just one interval is satisfied, then ok
  }

  return false
}

fun instantIsAtLeast30MinutesAwayFromLast(check: Instant, lastInstant: Instant): Boolean =
  check - lastInstant >= 30.minutes
//</editor-fold>

//<editor-fold desc="PASS-HASHING-SECTION">
fun hashed(plain: String): String {
  if (plain.isEmpty()) throw IllegalArgumentException("Password cannot be empty.")
  return BCrypt.hashpw(plain, BCrypt.gensalt())
}

fun plainMatchesHashed(plain: String, hashed: String): Boolean {
  if (hashed.isEmpty()) throw IllegalArgumentException("Hashed password cannot be empty.")
  return BCrypt.checkpw(plain, hashed)
}
//</editor-fold>

//<editor-fold desc="JWT-SECTION">
data class AppJwtClaims(
  val userId: Int,
  val isAdmin: Boolean,
  val expiresAt: Instant = (Clock.System.now() + Constants.DEFAULT_JWT_EXPIRATION_TIME.minutes)
) {
  companion object {
    const val USER_ID_KEY = "id"
    const val IS_ADMIN_KEY = "is_admin"
  }
}

object JwtGenerator {

  private val jwtAlgorithmSignSecret = "JWT_ALGORITHM_SIGN_SECRET"

  val verifier: JWTVerifier = JWT
    .require(Algorithm.HMAC256(jwtAlgorithmSignSecret))
    .build()

  fun generate(
    claims: AppJwtClaims
  ): String {
    return try {
      JWT.create()
        .withClaim(AppJwtClaims.USER_ID_KEY, claims.userId)
        .withClaim(AppJwtClaims.IS_ADMIN_KEY, claims.isAdmin)
        .withExpiresAt(claims.expiresAt.toJavaInstant())
        .sign(Algorithm.HMAC256(jwtAlgorithmSignSecret))
    } catch (e: JWTCreationException) {
      throw AppError("Error occurred while creating a JWT")
    }
  }
}
//</editor-fold>

//<editor-fold desc="IN-MEMORY-DATA-HANDLER">
object InMemoryDataHandler : DataHandler {
  private val users = mutableListOf<User>()
  private val points = mutableListOf<Point>()

  override suspend fun signupUser(dto: CreateUserRequestDTO, isAdmin: Boolean): Int {
    if (users.any { it.email == dto.email }) throw DataHandlingError("Email already exists")
    val nextId = users.size + 1
    users += User(
      id = nextId,
      name = dto.name,
      email = dto.email,
      hashedPassword = hashed(plain = dto.plainPassword),
      timeIntervals = dto.timeIntervals,
      isAdmin = isAdmin,
    )
    return nextId
  }

  override suspend fun loginUser(dto: CredentialsDTO): String = users.find { it.email == dto.email }.let {
    if (it == null) throw AuthenticationError("Email not exists")
    if (!plainMatchesHashed(
        plain = dto.plainPassword,
        hashed = it.hashedPassword
      )
    ) {
      throw AuthenticationError("Email or password doesn't match")
    }

    JwtGenerator.generate(AppJwtClaims(userId = it.id, isAdmin = it.isAdmin))
  }

  override suspend fun createPointFor(userId: Int): Int = users.find { it.id == userId }.let {
    if (it == null) throw DataHandlingError("Related user id not exists")

    val currentServerInstant = Clock.System.now()

    if (!instantIsInValidTimeInterval(currentServerInstant, it.timeIntervals))
      throw RuleViolatedError("Not matches: [instant Is Invalid Time Interval]")

    val relatedUserPoints = points.filter { p -> p.relatedUserId == userId }
    if (relatedUserPoints.isNotEmpty()) {
      if (!instantIsAtLeast30MinutesAwayFromLast(currentServerInstant, relatedUserPoints.last().instant))
        throw RuleViolatedError("Not matches: [instant Is At Least 30 Minutes Away From Last]")
    }

    val nextId = points.size + 1

    points += Point(
      id = nextId,
      relatedUserId = userId,
      instant = currentServerInstant
    )

    nextId
  }

  override suspend fun getAllUserPoints(userId: Int): List<GetPointRequestDTO> = points.mapNotNull {
    if (it.relatedUserId != userId) null
    else GetPointRequestDTO(it.id, it.relatedUserId, it.instant)
  }

  override suspend fun getAllUsers(): List<GetUserResponseDTO> =
    users.map { GetUserResponseDTO(it.id, it.name, it.email, it.timeIntervals, it.isAdmin) }

  override suspend fun getAllPoints(): List<GetPointRequestDTO> =
    points.map { GetPointRequestDTO(it.id, it.relatedUserId, it.instant) }

  override suspend fun deleteUser(userId: Int) {
    users.removeIf { it.id == userId }
    points.removeIf { it.relatedUserId == userId }
  }

  override suspend fun deletePoint(pointId: Int) {
    points.removeIf { it.id == pointId }
  }

  override suspend fun clearPoints() {
    points.clear()
  }

  override suspend fun clearUsers() {
    points.clear()
    users.clear()
  }
}
//</editor-fold>

//<editor-fold desc="KTOR-CONFIGURATION-SECTION">
fun Application.authenticationConfiguration() {
  install(Authentication) {
    jwt("flpoint-jwt-auth") {
      verifier(JwtGenerator.verifier)

      validate { jwtCredential ->
        val id = jwtCredential.payload.getClaim(AppJwtClaims.USER_ID_KEY).asInt()
        val isAdmin = jwtCredential.payload.getClaim(AppJwtClaims.IS_ADMIN_KEY).asBoolean()

        if (id != null && isAdmin != null) {
          JWTPrincipal(jwtCredential.payload)
        } else {
          null
        }
      }
    }
  }
}

fun Application.statusPagesConfiguration() {
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      return@exception when (val root = cause.customRootCause()) {
        is DataHandlingError -> call.respond(HttpStatusCode.InternalServerError, root.message ?: "DataHandlingError")
        is AuthenticationError -> call.respond(HttpStatusCode.Unauthorized, root.message ?: "AuthenticationError")
        is NoPrivilegeError -> call.respond(HttpStatusCode.Forbidden, root.message ?: "NoPrivilegeError")
        is RuleViolatedError -> call.respond(
          HttpStatusCode.UnprocessableEntity,
          root.message ?: "RuleViolatedError"
        )

        else -> {
          cause.printStackTrace()
          if (cause is BadRequestException) {
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "BadRequestException")
          } else {
            call.respond(HttpStatusCode.InternalServerError, "InternalServerError")
          }
        }
      }
    }
  }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleAsAuthenticatedAdmin(
  onSucceedAdminVerification: suspend () -> Unit = {}
) {
  val principal = call.principal<JWTPrincipal>()
  val isAdmin = principal?.payload?.getClaim(AppJwtClaims.IS_ADMIN_KEY)?.asBoolean() ?: false

  if (isAdmin) onSucceedAdminVerification()
  else throw NoPrivilegeError()
}

fun Application.serializationConfiguration() {
  install(ContentNegotiation) { json(Json { isLenient = false }) }
}

fun Application.routingConfiguration(dataHandler: DataHandler) {
  routing { routesHandlers(dataHandler) }
}

fun Application.initKtorConfiguration(dataHandler: DataHandler) {
  authenticationConfiguration()
  statusPagesConfiguration()
  serializationConfiguration()
  routingConfiguration(dataHandler)
}
//</editor-fold>

//<editor-fold desc="KTOR-ROUTES-HANDLERS-SECTION">
fun Routing.routesHandlers(dataHandler: DataHandler) {
  get("/health") { call.respondText("Hello from Kotlin/Ktor API!") }

  post("/register") {
    val dto = call.receive<CreateUserRequestDTO>()
    val result = dataHandler.signupUser(dto)
    call.respond(HttpStatusCode.Created, result)
  }

  post("/login") {
    val dto = call.receive<CredentialsDTO>()
    val jwt = dataHandler.loginUser(dto)
    call.respond(HttpStatusCode.OK, jwt)
  }

  authenticate("flpoint-jwt-auth") {
    get("admin-only") {
      return@get handleAsAuthenticatedAdmin {
        call.respond(HttpStatusCode.OK)
      }
    }

    post("/point") {
      val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(AppJwtClaims.USER_ID_KEY)?.asInt() ?: -1
      val result = dataHandler.createPointFor(userId)
      return@post call.respond(HttpStatusCode.Created, result)
    }

    get("/points") {
      val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(AppJwtClaims.USER_ID_KEY)?.asInt() ?: -1
      val result = dataHandler.getAllUserPoints(userId)
      return@get call.respond(HttpStatusCode.OK, result)
    }

    get("/admin/users") {
      return@get handleAsAuthenticatedAdmin {
        call.respond(HttpStatusCode.OK, dataHandler.getAllUsers())
      }
    }

    get("/admin/points") {
      return@get handleAsAuthenticatedAdmin {
        call.respond(HttpStatusCode.OK, dataHandler.getAllPoints())
      }
    }
  }
}
//</editor-fold>

fun main() {
  val t =
    LocalTime(hour = 24, minute = 0, second = 0).asInstant(today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
  println(t)
}