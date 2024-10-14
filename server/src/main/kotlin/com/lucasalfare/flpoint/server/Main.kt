/*
- the user must be created with at least 1 time interval;
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.lucasalfare.flpoint.server

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
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
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
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

fun validateName(name: String) {
  if (name.isBlank()) throw ValidationError("Name cannot be empty")
  if (name.length < 2) throw ValidationError("Name must have at least 2 characters")
}

fun validateEmail(email: String) {
  val emailRegex = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$".toRegex()
  if (!email.matches(emailRegex)) throw ValidationError("Invalid email format")
}

fun validatePassword(password: String) {
  if (password.length < Constants.DEFAULT_MIN_PASSWORD_LENGTH)
    throw ValidationError("Password must have at least ${Constants.DEFAULT_MIN_PASSWORD_LENGTH} characters")
}
//</editor-fold>

//<editor-fold desc="MODELING-SECTION">
class Constants {
  companion object {
    const val DEFAULT_MIN_PASSWORD_LENGTH = 4

    const val DEFAULT_LOWER_POINT_TOLERANCE_DURATION = 5 // minutes
    const val DEFAULT_HIGHER_POINT_TOLERANCE_DURATION = 5 // minutes
    const val DEFAULT_JWT_EXPIRATION_TIME = 10 // minutes

    const val DATABASE_SQLITE_URL = "jdbc:sqlite:./data.db"

    // in memory H2, for testing
    const val DATABASE_H2_URL = "jdbc:h2:mem:regular"
    const val DATABASE_H2_DRIVER = "org.h2.Driver"
  }
}

open class AppError(description: String) : Throwable(description)
class DataHandlingError(message: String = "DataHandlingError") : AppError(message)
class AuthenticationError(message: String = "AuthenticationError") : AppError(message)
class ValidationError(message: String = "ValidationError") : AppError(message)
class RuleViolatedError(message: String = "RulesNotMatched") : AppError(message)
class NoPrivilegeError(message: String = "NoPrivilegeError") : AppError(message)
// ...

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
) {

  fun toUserDto() = UserDTO(
    id, name, email, timeIntervals, isAdmin
  )
}

data class Point(
  val id: Int,
  val relatedUserId: Int,
  val localDateTime: LocalDateTime
) {

  fun toPointDto() = PointDTO(
    id, relatedUserId, localDateTime
  )
}

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
      throw ValidationError("At least one time interval must be provided")
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
data class UserDTO(
  val id: Int,
  val name: String,
  val email: String,
  val timeIntervals: List<TimeInterval>,
  val isAdmin: Boolean
)

@Serializable
data class PointDTO(
  val id: Int,
  val relatedUserId: Int,
  val localDateTime: LocalDateTime
)

interface DataCRUD {

  suspend fun createUser(
    name: String,
    email: String,
    hashedPassword: String,
    timeIntervals: List<TimeInterval>,
    isAdmin: Boolean
  ): Int

  suspend fun getUser(id: Int): User?

  suspend fun getUser(email: String): User?

  suspend fun updateUser(
    id: Int,
    name: String? = null,
    email: String? = null,
    hashedPassword: String? = null,
    timeIntervals: List<TimeInterval>? = null,
    isAdmin: Boolean? = null
  ): Boolean

  suspend fun deleteUser(id: Int): Boolean

  suspend fun clearUsers(): Boolean

  suspend fun createPoint(relatedUserId: Int, localDateTime: LocalDateTime): Int

  suspend fun getPoint(id: Int): Point?

  suspend fun getPointsByUserId(userId: Int): List<Point>?

  suspend fun updatePoint(id: Int): Boolean

  suspend fun deletePoint(id: Int): Boolean

  suspend fun clearPoints(): Boolean
}
//</editor-fold>

//<editor-fold desc="EXPOSED-SCHEMA">
object Users : IntIdTable("Users") {
  val name = varchar("name", 255)
  val email = varchar("email", 255).uniqueIndex()
  val hashedPassword = varchar("hashed_password", 255)
  val isAdmin = bool("is_admin").default(false)
  val timeIntervals = array<TimeInterval>("time_intervals")
}

object Points : IntIdTable("Points") {
  val relatedUserId = integer("related_user_id").references(Users.id)
  val localDateTime = datetime("local_date_time")
}
//</editor-fold>

//<editor-fold desc="EXPOSED-DATA-CRUD">
object ExposedDataCRUD : DataCRUD {

  override suspend fun createUser(
    name: String,
    email: String,
    hashedPassword: String,
    timeIntervals: List<TimeInterval>,
    isAdmin: Boolean
  ): Int = AppDB.exposedQuery {
    try {
      Users.insertAndGetId {
        it[Users.name] = name
        it[Users.email] = email
        it[Users.hashedPassword] = hashedPassword
        it[Users.timeIntervals] = timeIntervals
        it[Users.isAdmin] = isAdmin
      }.value
    } catch (e: Exception) {
      throw DataHandlingError("Could not to create user")
    }
  }

  override suspend fun getUser(id: Int): User? = AppDB.exposedQuery {
    try {
      Users.selectAll().where { Users.id eq id }.singleOrNull().let {
        if (it == null) null
        else User(
          id = it[Users.id].value,
          name = it[Users.name],
          email = it[Users.email],
          hashedPassword = it[Users.hashedPassword],
          timeIntervals = it[Users.timeIntervals],
          isAdmin = it[Users.isAdmin]
        )
      }
    } catch (e: Exception) {
      throw DataHandlingError("Error selecting user from database")
    }
  }

  override suspend fun getUser(email: String): User? = AppDB.exposedQuery {
    try {
      Users.selectAll().where { Users.email eq email }.singleOrNull().let {
        if (it == null) null
        else User(
          id = it[Users.id].value,
          name = it[Users.name],
          email = it[Users.email],
          hashedPassword = it[Users.hashedPassword],
          timeIntervals = it[Users.timeIntervals],
          isAdmin = it[Users.isAdmin]
        )
      }
    } catch (e: Exception) {
      throw DataHandlingError("Error selecting user from database")
    }
  }

  override suspend fun updateUser(
    id: Int,
    name: String?,
    email: String?,
    hashedPassword: String?,
    timeIntervals: List<TimeInterval>?,
    isAdmin: Boolean?
  ): Boolean = AppDB.exposedQuery {
    try {
      Users.update(where = { Users.id eq id }) {
        if (name != null) it[Users.name] = name
        if (email != null) it[Users.email] = email
        if (hashedPassword != null) it[Users.hashedPassword] = hashedPassword
        if (timeIntervals != null) it[Users.timeIntervals] = timeIntervals
        if (isAdmin != null) it[Users.isAdmin] = isAdmin
      } > 0
    } catch (e: Exception) {
      throw DataHandlingError("Error updating user by ID")
    }
  }

  override suspend fun deleteUser(id: Int): Boolean = AppDB.exposedQuery {
    try {
      Users.deleteWhere { Users.id eq id } > 0
    } catch (e: Exception) {
      throw DataHandlingError("Error deleting user by ID")
    }
  }

  override suspend fun clearUsers(): Boolean = AppDB.exposedQuery {
    try {
      Users.deleteAll() > 0
    } catch (e: Exception) {
      throw DataHandlingError("Error clearing users")
    }
  }

  override suspend fun createPoint(relatedUserId: Int, localDateTime: LocalDateTime): Int = AppDB.exposedQuery {
    try {
      Points.insertAndGetId {
        it[Points.relatedUserId] = relatedUserId
        it[Points.localDateTime] = localDateTime
      }.value
    } catch (e: Exception) {
      throw DataHandlingError("Error inserting point")
    }
  }

  override suspend fun getPoint(id: Int): Point? = AppDB.exposedQuery {
    try {
      Points.selectAll().where { Points.id eq id }.singleOrNull().let {
        if (it == null) {
          null
        } else {
          Point(
            id = it[Points.id].value,
            relatedUserId = it[Points.relatedUserId],
            localDateTime = it[Points.localDateTime]
          )
        }
      }
    } catch (e: Exception) {
      throw DataHandlingError("Was not possible to select the desired point by ID")
    }
  }

  override suspend fun getPointsByUserId(userId: Int): List<Point>? = AppDB.exposedQuery {
    try {
      Points
        .selectAll()
        .where { Points.relatedUserId eq userId }
        .map {
          Point(it[Points.id].value, it[Points.relatedUserId], it[Points.localDateTime])
        }.let {
          it.ifEmpty { null }
        }
    } catch (e: Exception) {
      throw DataHandlingError("Was not possible select points by the desired user ID")
    }
  }

  override suspend fun updatePoint(id: Int): Boolean {
    return false
  }

  override suspend fun deletePoint(id: Int): Boolean = AppDB.exposedQuery {
    try {
      Points.deleteWhere { Points.id eq id } == 1
    } catch (e: Exception) {
      throw DataHandlingError("Was not possible to delete point by ID")
    }
  }

  override suspend fun clearPoints(): Boolean = AppDB.exposedQuery {
    try {
      Points.deleteAll() >= 0
    } catch (e: Exception) {
      throw DataHandlingError("Was not possible to clear all points")
    }
  }
}
//</editor-fold>

//<editor-fold desc="DATA-USECASES">
object DataUsecases {

  suspend fun signupUser(createUserRequestDTO: CreateUserRequestDTO, isAdmin: Boolean = false): Int {
    val nextHashedPassword = hashed(createUserRequestDTO.plainPassword)
    return ExposedDataCRUD.createUser(
      name = createUserRequestDTO.name,
      email = createUserRequestDTO.email,
      hashedPassword = nextHashedPassword,
      timeIntervals = createUserRequestDTO.timeIntervals,
      isAdmin = isAdmin
    )
  }

  suspend fun loginUser(credentialsDTO: CredentialsDTO): String = ExposedDataCRUD.getUser(credentialsDTO.email).let {
    if (it == null) throw AuthenticationError("User doesn't exists")
    return@let JwtGenerator.generate(AppJwtClaims(userId = it.id, isAdmin = it.isAdmin))
  }

  suspend fun doPoint(userId: Int): Int {
    // TODO: we need to user the user TZ instead. But, to use it, it needs to be stored in DB
    val nextPointLocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)

    // TODO: validate if the above LDT is in all user timeIntervals
    // TODO: validate if the above LDT is at least 30 min away from last registered point
    // TODO: if none rules was ok, then throw RuleViolatedError()

    // if rules was verified, then store
    return ExposedDataCRUD.createPoint(relatedUserId = userId, localDateTime = nextPointLocalDateTime)
  }

  suspend fun getUserPoints(userId: Int): List<PointDTO>? {
    return ExposedDataCRUD.getPointsByUserId(userId)?.map { it.toPointDto() }
  }
}
//</editor-fold>

//<editor-fold desc="EXPOSED-DB-CONNECTION">
object AppDB {

  private lateinit var hikariDataSource: HikariDataSource

  internal val DB by lazy { Database.connect(hikariDataSource) }

  fun initialize(
    jdbcUrl: String,
    jdbcDriverClassName: String,
    username: String,
    password: String,
    maximumPoolSize: Int,
    onFirstTransactionCallback: () -> Unit = {}
  ) {
    hikariDataSource = createHikariDataSource(
      jdbcUrl = jdbcUrl,
      jdbcDriverClassName = jdbcDriverClassName,
      username = username,
      password = password,
      maximumPoolSize = maximumPoolSize
    )

    transaction(DB) {
      onFirstTransactionCallback()
    }
  }

  suspend fun <T> exposedQuery(queryCodeBlock: suspend () -> T): T =
    newSuspendedTransaction(context = Dispatchers.IO, db = DB) {
      queryCodeBlock()
    }

  private fun createHikariDataSource(
    jdbcUrl: String,
    jdbcDriverClassName: String,
    username: String,
    password: String,
    maximumPoolSize: Int,
  ): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
      if (jdbcDriverClassName == Constants.DATABASE_SQLITE_URL)
        this.transactionIsolation = IsolationLevel.TRANSACTION_SERIALIZABLE.name

      this.jdbcUrl = jdbcUrl
      this.driverClassName = jdbcDriverClassName
      this.username = username
      this.password = password
      this.maximumPoolSize = maximumPoolSize
      this.isAutoCommit = true
      this.validate()
    }

    return HikariDataSource(hikariConfig)
  }
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

fun ApplicationCall.getAppJwtClaims(): AppJwtClaims? {
  val principal = principal<JWTPrincipal>() ?: return null
  val userId = principal.payload.getClaim(AppJwtClaims.USER_ID_KEY) ?: return null
  val isAdmin = principal.payload.getClaim(AppJwtClaims.IS_ADMIN_KEY) ?: return null
  val expiresAt = principal.payload.expiresAt
  return AppJwtClaims(userId.asInt(), isAdmin.asBoolean(), expiresAt.toInstant().toKotlinInstant())
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
        is ValidationError -> call.respond(HttpStatusCode.Unauthorized, root.message ?: "ValidationError")
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

fun Application.initKtorConfiguration() {
  authenticationConfiguration()
  statusPagesConfiguration()
  serializationConfiguration()
  routing { routesHandlers() }
}
//</editor-fold>

//<editor-fold desc="KTOR-ROUTES-HANDLERS-SECTION">
fun Routing.routesHandlers() {
  get("/health") { call.respondText("Hello from Kotlin/Ktor API!") }

  post("/register") {
    val dto = call.receive<CreateUserRequestDTO>()
    val result = DataUsecases.signupUser(dto)
    return@post call.respond(status = HttpStatusCode.Created, message = result)
  }

  post("/login") {
    val dto = call.receive<CredentialsDTO>()
    val result = DataUsecases.loginUser(dto)
    return@post call.respond(HttpStatusCode.OK, result)
  }

  authenticate("flpoint-jwt-auth") {
    get("/admin-only") {
      // only for testing
      return@get handleAsAuthenticatedAdmin {
        call.respond(HttpStatusCode.OK)
      }
    }

    patch("/users") {
      // TODO: handle "update password" logic
    }

    // Create point route
    post("/point") {
      val claims = call.getAppJwtClaims() ?: throw AppError("Error retrieving JWT claims!")
      val result = DataUsecases.doPoint(claims.userId)
      return@post call.respond(HttpStatusCode.Created, result)
    }

    // Route for user getting only his own points
    get("/points") {
      val claims = call.getAppJwtClaims() ?: throw AppError("Error retrieving JWT claims!")
      val result = DataUsecases.getUserPoints(claims.userId)
        ?: return@get call.respond(HttpStatusCode.NotFound, "No points found for requested user ID")
      return@get call.respond(HttpStatusCode.OK, result)
    }

    get("/admin/users") {
      return@get handleAsAuthenticatedAdmin {

      }
    }

    delete("/admin/users/{id}") {

    }

    get("/admin/points") {
      return@get handleAsAuthenticatedAdmin {

      }
    }
  }
}
//</editor-fold>