/**
 * pt-br: Arquivão gigante, só quero sair codando agora, tô nem aí 😳
 *
 * en-us: Giant single file, now I just want to start coding, I don't care 😳
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
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import kotlin.time.Duration.Companion.minutes

//<editor-fold desc="EXTENSIONS-SECTION">
// TODO: check if was found a cyclic reference to avoid infinite loops
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
fun instantIsInValidTimeInterval(check: Instant, user: User): Boolean {
  // we check the instant as a local date time in the stored user TZ
  val checkLocal = check.toLocalDateTime(user.timeZone)

  // time intervals are just flat hours in a day, e.g.: "enter=8:00 morning; exit=14:00 afternoon"
  // due to this, they are not taking care about TZ
  for (interval in user.timeIntervals) {
    // if the checking time is inside of at least one of intervals, then early return true
    if (checkLocal.time in (interval.enter..interval.exit)) {
      return true
    }
  }

  return false
}

// TODO: include rule about if checking instant is in tolerated times
// TODO: this should be implemented in modeling and in Usecases:
// TODO: if outside a tolerated range, then add some flag to the user...

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

    const val DEFAULT_ENTER_TOLERANCE_MINUTES = 5 // minutes
    const val DEFAULT_EXIT_TOLERANCE_MINUTES = 5 // minutes
    const val DEFAULT_JWT_EXPIRATION_TIME = 10 // minutes

    const val DATABASE_SQLITE_URL = "jdbc:sqlite:./data.db"

    // in memory H2, for testing
//    const val DATABASE_H2_URL = "jdbc:h2:mem:regular"
    const val DATABASE_H2_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;"
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
) {

  init {
    if (exit < enter) throw ValidationError("Exit time is earlier than Enter time!")
    // TODO: validate if the difference between enter/exit is less/higher than something
  }

  /*
  we are keeping some string-parsing functions to avoid using extra tables
  for time intervals and to avoid using "array" column type, once it is
  only compatible with Postgre and H2 dialects.

 Look "Users" table in the Exposed Schema to see the [timeIntervalsStringList]
 column.
   */
  companion object {
    /*
    hh:mm/hh:mm
     */
    fun fromString(s: String): TimeInterval {
      val split = s.split("/")
      if (split.size != 2) throw AppError("Can not to build TimeInterval from the string [$s].")

      try {
        val enterTime = split[0].split(":")
        val exitTime = split[1].split(":")

        val nextEnter = LocalTime(hour = enterTime[0].toInt(), minute = enterTime[1].toInt())
        val nextExit = LocalTime(hour = exitTime[0].toInt(), minute = exitTime[1].toInt())

        return TimeInterval(
          enter = nextEnter,
          exit = nextExit
        )
      } catch (e: Exception) {
        throw AppError("Was not possible to parse and build a TimeInterval instance from the string [$s].")
      }
    }

    /*
    [hh:mm/hh:mm, ...]
     */
    fun fromStringList(sList: String): List<TimeInterval> {
      val sanitized = sList.replace("[", "").replace("]", "").replace(" ", "").split(",")
      return sanitized.map { fromString(it) }
    }

    fun listToString(timeIntervals: List<TimeInterval>): String = timeIntervals.map { it.toString() }.toString()
  }

  /*
  hh:mm,hh:mm
  */
  override fun toString() = "${enter.hour}:${enter.minute}/${exit.hour}:${exit.minute}"
}

data class User(
  val id: Int,
  val name: String,
  val email: String,
  val hashedPassword: String,
  val timeIntervals: List<TimeInterval>,
  val timeZone: TimeZone,
  val isAdmin: Boolean
) {

  init {
    if (!isAdmin && timeIntervals.isEmpty())
      throw ValidationError("A non-admin user must have at lease 1 time interval!")
  }

  fun toUserDto() = UserDTO(
    id, name, email, timeIntervals, isAdmin
  )
}

data class Point(
  val id: Int,
  val relatedUserId: Int,
  val instant: Instant
) {

  fun toPointDto() = PointDTO(
    id, relatedUserId, instant
  )
}

@Serializable
data class CreateUserRequestDTO(
  val name: String,
  val email: String,
  val plainPassword: String,
  val timeIntervals: List<TimeInterval>,
  val timeZone: TimeZone = TimeZone.UTC
) {
  init {
    validateName(name)
    validateEmail(email)
    validatePassword(plainPassword)
  }
}

@Serializable
data class UpdateUserPasswordRequestDTO(
  val currentPlainPassword: String,
  val newPlainPassword: String,
) {

  init {
    validatePassword(currentPlainPassword)
    validatePassword(newPlainPassword)
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
  val instant: Instant
)

interface DataCRUD {

  suspend fun createUser(
    name: String,
    email: String,
    hashedPassword: String,
    timeIntervals: List<TimeInterval>,
    timeZone: TimeZone,
    isAdmin: Boolean
  ): Int

  suspend fun getUser(id: Int): User?

  suspend fun getUser(email: String): User?

  suspend fun getAllUsers(): List<User>

  suspend fun updateUser(
    id: Int,
    name: String? = null,
    email: String? = null,
    hashedPassword: String? = null,
    timeIntervals: List<TimeInterval>? = null,
    timeZone: TimeZone? = null,
    isAdmin: Boolean? = null
  ): Boolean

  suspend fun deleteUser(id: Int): Boolean

  suspend fun clearUsers(): Boolean

  suspend fun createPoint(relatedUserId: Int, instant: Instant): Int

  suspend fun getPoint(id: Int): Point?

  suspend fun getPointsByUserId(userId: Int): List<Point>?

  suspend fun getAllPoints(): List<Point>

  suspend fun deletePoint(id: Int): Boolean

  suspend fun clearPoints(): Boolean
}
//</editor-fold>

//<editor-fold desc="EXPOSED-SCHEMA">
object Users : IntIdTable("Users") {
  val name = varchar("name", 255)
  val email = varchar("email", 255).uniqueIndex()
  val hashedPassword = varchar("hashed_password", 255)
  val timeIntervalsStringList = text("time_intervals")
  val timeZone = text("time_zone")
  val isAdmin = bool("is_admin").default(false)
}

object Points : IntIdTable("Points") {
  val relatedUserId = integer("related_user_id").references(Users.id)
  val instant = timestamp("instant")
}
//</editor-fold>

//<editor-fold desc="EXPOSED-DATA-CRUD">
object ExposedDataCRUD : DataCRUD {

  override suspend fun createUser(
    name: String,
    email: String,
    hashedPassword: String,
    timeIntervals: List<TimeInterval>,
    timeZone: TimeZone,
    isAdmin: Boolean
  ): Int = AppDB.safeQuery(onFailureThrowable = DataHandlingError("Could not to create user")) {
    Users.insertAndGetId {
      it[Users.name] = name
      it[Users.email] = email
      it[Users.hashedPassword] = hashedPassword
      it[Users.timeIntervalsStringList] = TimeInterval.listToString(timeIntervals)
      it[Users.timeZone] = timeZone.toString()
      it[Users.isAdmin] = isAdmin
    }.value
  }

  override suspend fun getUser(id: Int): User? =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Error selecting user from database")) {
      Users.selectAll().where { Users.id eq id }.singleOrNull().let {
        if (it == null) null
        else User(
          id = it[Users.id].value,
          name = it[Users.name],
          email = it[Users.email],
          hashedPassword = it[Users.hashedPassword],
          timeIntervals = TimeInterval.fromStringList(it[Users.timeIntervalsStringList]),
          timeZone = TimeZone.of(it[Users.timeZone]),
          isAdmin = it[Users.isAdmin]
        )
      }
    }

  override suspend fun getUser(email: String): User? =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Error selecting user from database")) {
      Users.selectAll().where { Users.email eq email }.singleOrNull().let {
        if (it == null) null
        else User(
          id = it[Users.id].value,
          name = it[Users.name],
          email = it[Users.email],
          hashedPassword = it[Users.hashedPassword],
          timeIntervals = TimeInterval.fromStringList(it[Users.timeIntervalsStringList]),
          timeZone = TimeZone.of(it[Users.timeZone]),
          isAdmin = it[Users.isAdmin]
        )
      }
    }

  override suspend fun getAllUsers(): List<User> = AppDB.safeQuery(
    onFailureThrowable = DataHandlingError("Error trying to retrieve all application users")
  ) {
    Users.selectAll().map {
      User(
        id = it[Users.id].value,
        name = it[Users.name],
        email = it[Users.email],
        hashedPassword = it[Users.hashedPassword],
        timeIntervals = TimeInterval.fromStringList(it[Users.timeIntervalsStringList]),
        timeZone = TimeZone.of(it[Users.timeZone]),
        isAdmin = it[Users.isAdmin]
      )
    }
  }

  override suspend fun updateUser(
    id: Int,
    name: String?,
    email: String?,
    hashedPassword: String?,
    timeIntervals: List<TimeInterval>?,
    timeZone: TimeZone?,
    isAdmin: Boolean?
  ): Boolean = AppDB.safeQuery(onFailureThrowable = DataHandlingError("Error updating user by ID")) {
    Users.update(where = { Users.id eq id }) {
      if (name != null) it[Users.name] = name
      if (email != null) it[Users.email] = email
      if (hashedPassword != null) it[Users.hashedPassword] = hashedPassword
      if (timeIntervals != null) it[Users.timeIntervalsStringList] = TimeInterval.listToString(timeIntervals)
      if (timeZone != null) it[Users.timeZone] = timeZone.toString()
      if (isAdmin != null) it[Users.isAdmin] = isAdmin
    } > 0
  }

  override suspend fun deleteUser(id: Int): Boolean =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Error deleting user by ID")) {
      Users.deleteWhere { Users.id eq id } > 0
    }

  override suspend fun clearUsers(): Boolean =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Error clearing users")) {
      Users.deleteAll() > 0
    }

  override suspend fun createPoint(relatedUserId: Int, instant: Instant): Int =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Error inserting point")) {
      Points.insertAndGetId {
        it[Points.relatedUserId] = relatedUserId
        it[Points.instant] = instant
      }.value
    }

  override suspend fun getPoint(id: Int): Point? =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Was not possible to select the desired point by ID")) {
      Points.selectAll().where { Points.id eq id }.singleOrNull().let {
        if (it == null) {
          null
        } else {
          Point(
            id = it[Points.id].value,
            relatedUserId = it[Points.relatedUserId],
            instant = it[Points.instant]
          )
        }
      }
    }

  override suspend fun getPointsByUserId(userId: Int): List<Point>? =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Was not possible select points by the desired user ID")) {
      Points
        .selectAll()
        .where { Points.relatedUserId eq userId }
        .map {
          Point(
            id = it[Points.id].value,
            relatedUserId = it[Points.relatedUserId],
            instant = it[Points.instant]
          )
        }.let {
          it.ifEmpty { null }
        }
    }

  override suspend fun getAllPoints(): List<Point> = AppDB.safeQuery(
    onFailureThrowable = DataHandlingError("Error trying to retrieve all points of the application.")
  ) {
    Points.selectAll().map {
      Point(
        id = it[Points.id].value,
        relatedUserId = it[Points.relatedUserId],
        instant = it[Points.instant]
      )
    }
  }

  override suspend fun deletePoint(id: Int): Boolean =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Was not possible to delete point by ID")) {
      Points.deleteWhere { Points.id eq id } == 1
    }

  override suspend fun clearPoints(): Boolean =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Was not possible to clear all points")) {
      Points.deleteAll() >= 0
    }
}
//</editor-fold>

//<editor-fold desc="DATA-USECASES">
object AppUsecases {
  suspend fun signupUser(createUserRequestDTO: CreateUserRequestDTO, isAdmin: Boolean = false): Int {
    val nextUser = User(
      id = -1, // not defined here
      name = createUserRequestDTO.name,
      email = createUserRequestDTO.email,
      hashedPassword = hashed(createUserRequestDTO.plainPassword),
      timeIntervals = createUserRequestDTO.timeIntervals,
      timeZone = createUserRequestDTO.timeZone,
      isAdmin = isAdmin
    )

    return ExposedDataCRUD.createUser(
      name = nextUser.name,
      email = nextUser.email,
      hashedPassword = nextUser.hashedPassword,
      timeIntervals = nextUser.timeIntervals,
      timeZone = nextUser.timeZone,
      isAdmin = nextUser.isAdmin
    )
  }

  suspend fun loginUser(credentialsDTO: CredentialsDTO): String = ExposedDataCRUD.getUser(credentialsDTO.email).let {
    if (it == null) throw AuthenticationError("User doesn't exists")

    if (!plainMatchesHashed(credentialsDTO.plainPassword, it.hashedPassword)) {
      throw AuthenticationError("Bad credentials")
    }

    return@let JwtGenerator.generate(AppJwtClaims(userId = it.id, isAdmin = it.isAdmin))
  }

  suspend fun doPoint(userId: Int): Int {
    ExposedDataCRUD.getUser(userId).let {
      if (it == null) throw AppError("User not found")

      val generatedInstant = Clock.System.now()
      if (!instantIsInValidTimeInterval(check = generatedInstant, user = it)) {
        throw RuleViolatedError("Tried to do point in a time that is not in any of the user time intervals!")
      }

      // we assume the list can not be null, it can only be empty, because a user with [userId] was verified above
      return ExposedDataCRUD.getPointsByUserId(it.id).let { instants ->
        if (instants != null) {
          if (instants.isNotEmpty()) {
            val lastInstant = instants.last().instant
            if (!instantIsAtLeast30MinutesAwayFromLast(check = generatedInstant, lastInstant = lastInstant)) {
              throw RuleViolatedError("Tried to create a point before at least 30 min from last point!")
            }
          }
        }

        ExposedDataCRUD.createPoint(
          relatedUserId = userId,
          instant = generatedInstant
        )
      }
    }
  }

  suspend fun getUserPoints(userId: Int): List<PointDTO>? {
    return ExposedDataCRUD.getPointsByUserId(userId)?.map { it.toPointDto() }
  }

  suspend fun getAllAppPoints(): List<PointDTO> = ExposedDataCRUD.getAllPoints().map { it.toPointDto() }

  suspend fun updateUserPassword(userId: Int, currentPlainPassword: String, newPlainPassword: String): Boolean =
    ExposedDataCRUD.getUser(userId).let { user: User? ->
      if (user == null) throw AppError("User not found by ID")

      if (!plainMatchesHashed(plain = currentPlainPassword, hashed = user.hashedPassword)) {
        throw AuthenticationError("Can not to update password. Current password doesn't match!")
      }

      ExposedDataCRUD.updateUser(
        id = userId,
        hashedPassword = hashed(newPlainPassword)
      )
    }

  suspend fun updateUserTimeIntervals(userId: Int, newIntervals: List<TimeInterval>): Boolean =
    ExposedDataCRUD.getUser(userId).let { user: User? ->
      if (user == null) throw AppError("User was not found by ID")
      if (newIntervals.isEmpty()) throw RuleViolatedError("Can not to update user time intervals: new intervals list is empty")

      ExposedDataCRUD.updateUser(id = userId, timeIntervals = newIntervals)
    }

  suspend fun getAllAppUsers(): List<UserDTO> = ExposedDataCRUD.getAllUsers().map { it.toUserDto() }

  suspend fun deleteUser(userId: Int): Boolean = ExposedDataCRUD.deleteUser(userId)
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

  fun isDatabaseConnected(): Boolean {
    return this::hikariDataSource.isInitialized && !hikariDataSource.isClosed
  }

  private suspend fun <T> exposedQuery(queryCodeBlock: suspend () -> T): T =
    newSuspendedTransaction(context = Dispatchers.IO, db = DB) {
      queryCodeBlock()
    }

  suspend fun <T> safeQuery(
    onFailureThrowable: Throwable? = null,
    queryFunction: suspend () -> T
  ): T = exposedQuery {
    try {
      queryFunction()
    } catch (e: Exception) {
      if (onFailureThrowable == null) throw Throwable("general error")
      else throw onFailureThrowable
    }
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
        is ValidationError -> call.respond(HttpStatusCode.UnprocessableEntity, root.message ?: "ValidationError")
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
  //<editor-fold desc="PUBLIC-ROUTES">
  // global root health route
  get("/health") { call.respondText("Hello from Kotlin/Ktor API!") }

  // used for signup an user
  post("/register") {
    val dto = call.receive<CreateUserRequestDTO>()
    val result = AppUsecases.signupUser(dto)
    return@post call.respond(status = HttpStatusCode.Created, message = result)
  }

  // used for logging in a existing user
  post("/login") {
    val dto = call.receive<CredentialsDTO>()
    val result = AppUsecases.loginUser(dto)
    return@post call.respond(HttpStatusCode.OK, result)
  }
  //</editor-fold>

  //<editor-fold desc="UNDER-AUTH-ROUTES">
  authenticate("flpoint-jwt-auth") {
    //<editor-fold desc="USER-ROUTES">
    // used to update current password
    patch("/users/update-password") {
      val claims = call.getAppJwtClaims() ?: throw AppError("Error retrieving JWT claims!")
      val receivedCurrentPlainPassword = call.receive<UpdateUserPasswordRequestDTO>()
      val result = AppUsecases.updateUserPassword(
        userId = claims.userId,
        currentPlainPassword = receivedCurrentPlainPassword.currentPlainPassword,
        newPlainPassword = receivedCurrentPlainPassword.newPlainPassword
      )

      return@patch call.respond(HttpStatusCode.OK, result)
    }

    // used to create point
    post("/users/point") {
      val claims = call.getAppJwtClaims() ?: throw AppError("Error retrieving JWT claims!")
      val result = AppUsecases.doPoint(claims.userId)
      return@post call.respond(HttpStatusCode.Created, result)
    }

    // Route for user getting only his own points
    get("/users/points") {
      val claims = call.getAppJwtClaims() ?: throw AppError("Error retrieving JWT claims!")
      val result = AppUsecases.getUserPoints(claims.userId)
        ?: return@get call.respond(HttpStatusCode.NotFound, "No points found for requested user ID")
      return@get call.respond(HttpStatusCode.OK, result)
    }
    //</editor-fold>

    //<editor-fold desc="ADMIN-ONLY-ROUTES">
    // admin health route
    get("/admin/health") {
      return@get handleAsAuthenticatedAdmin {
        call.respond(HttpStatusCode.OK)
      }
    }

    // used to get all database users
    get("/admin/users") {
      return@get handleAsAuthenticatedAdmin {
        val result = AppUsecases.getAllAppUsers()
        return@handleAsAuthenticatedAdmin call.respond(HttpStatusCode.OK, result)
      }
    }

    // used to update the time intervals of the {id} user
    patch("/admin/users/{id}/update-time-intervals") {
      return@patch handleAsAuthenticatedAdmin {
        val userId = call.parameters["id"] ?: throw AppError("Missing path parameter user ID")
        val receivedTimeIntervals = call.receive<List<TimeInterval>>()
        val result = AppUsecases.updateUserTimeIntervals(userId.toInt(), receivedTimeIntervals)
        return@handleAsAuthenticatedAdmin call.respond(HttpStatusCode.OK, result)
      }
    }

    // used to delete the {id} user
    delete("/admin/users/{id}") {
      return@delete handleAsAuthenticatedAdmin {
        val userId = call.parameters["id"] ?: throw AppError("Missing user ID")
        val result = AppUsecases.deleteUser(userId.toInt())
        return@handleAsAuthenticatedAdmin call.respond(HttpStatusCode.OK, result)
      }
    }

    // used to retrieve all the points of the database
    get("/admin/points") {
      return@get handleAsAuthenticatedAdmin {
        val result = AppUsecases.getAllAppPoints()
        return@handleAsAuthenticatedAdmin call.respond(HttpStatusCode.OK, result)
      }
    }
    //</editor-fold>
  }
  //</editor-fold>
}
//</editor-fold>