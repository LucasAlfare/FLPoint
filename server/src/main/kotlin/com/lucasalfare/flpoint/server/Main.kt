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
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

//<editor-fold desc="EXTENSIONS-SECTION">
fun Throwable.customRootCause(): Throwable {
  val visited = mutableSetOf<Throwable>()
  var current = this

  while (current.cause != null && current.cause !in visited) {
    visited += current
    current = current.cause!!
  }

  return current
}
//</editor-fold>

//<editor-fold desc="PASS-HASHING-SECTION">
fun hashed(plain: String): String {
  if (plain.isEmpty()) throw RuleViolatedError("Password cannot be empty.")
  return BCrypt.hashpw(plain, BCrypt.gensalt())
}

fun plainMatchesHashed(plain: String, hashed: String): Boolean {
  if (hashed.isEmpty()) throw RuleViolatedError("Hashed password cannot be empty.")
  return BCrypt.checkpw(plain, hashed)
}
//</editor-fold>

//<editor-fold desc="RULES-SECTION">
fun instantIsAtLeast10SecondsAwayFromLast(check: Instant, lastInstant: Instant): Boolean =
  check - lastInstant >= 10.seconds

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
    val logger: Logger = LoggerFactory.getLogger("App")!!

    const val DEFAULT_MIN_PASSWORD_LENGTH = 4

    const val DEFAULT_JWT_EXPIRATION_TIME = 10 // minutes

    const val DATABASE_SQLITE_URL = "jdbc:sqlite:./data.db"
    const val DATABASE_SQLITE_DRIVER = "org.sqlite.JDBC"

    // in memory H2, for testing
//    const val DATABASE_H2_URL = "jdbc:h2:mem:regular"
//    const val DATABASE_H2_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;"
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

data class User(
  val id: Int,
  val name: String,
  val email: String,
  val hashedPassword: String,
  val timeZone: TimeZone,
  val isAdmin: Boolean
) {
  fun toUserDto() = UserDTO(
    id, name, email, timeZone, isAdmin
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
data class LoginResponseDTO(
  val jwt: String,
  val userDTO: UserDTO
)

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
  val timeZone: TimeZone,
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
    timeZone: TimeZone,
    isAdmin: Boolean
  ): User

  suspend fun getUser(id: Int): User?

  suspend fun getUser(email: String): User?

  suspend fun getAllUsers(): List<User>

  suspend fun updateUser(
    id: Int,
    name: String? = null,
    email: String? = null,
    hashedPassword: String? = null,
    timeZone: TimeZone? = null,
    isAdmin: Boolean? = null
  ): Boolean

  suspend fun deleteUser(id: Int): Boolean

  suspend fun clearUsers(): Boolean

  suspend fun createPoint(relatedUserId: Int, instant: Instant): Int

  suspend fun getPoint(id: Int): Point?

  suspend fun getAllPointsByUserId(userId: Int): List<Point>

  suspend fun getAllPoints(): List<Point>

  suspend fun deletePoint(id: Int): Boolean

  suspend fun clearPoints(): Boolean

  suspend fun createJwtBlackListed(jwt: String, expiresAt: Instant)

  suspend fun jwtBlackListContains(jwt: String): Boolean

  suspend fun clearJwtBlackList()
}
//</editor-fold>

//<editor-fold desc="EXPOSED-SCHEMA">
object Users : IntIdTable("Users") {
  val name = varchar("name", 255)
  val email = varchar("email", 255).uniqueIndex()
  val hashedPassword = varchar("hashed_password", 255)
  val timeZone = text("time_zone")
  val isAdmin = bool("is_admin").default(false)
}

object Points : IntIdTable("Points") {
  val relatedUserId = integer("related_user_id").references(Users.id)
  val instant = timestamp("instant")
}

// TODO: include reference to user that holds this jwt
object JwtBlacklist : IntIdTable("JwtBlackList") {
  val jwt = text("jwt").uniqueIndex()
  val expiresAt = timestamp("expires_at").index()
}
//</editor-fold>

//<editor-fold desc="EXPOSED-DATA-CRUD">
// as we are using only this [DataCRUD] impl, it is an object instead of a
// class, but is an object just for convenience.
object ExposedDataCRUD : DataCRUD {

  override suspend fun createUser(
    name: String,
    email: String,
    hashedPassword: String,
    timeZone: TimeZone,
    isAdmin: Boolean
  ): User = AppDB.safeQuery(
    onFailureThrowable = DataHandlingError("Could not to create user")
  ) {
    // TODO: "insertReturning" is not supported by H2, but others can do. In future implement for both.

    val id = Users.insertAndGetId {
      it[Users.name] = name
      it[Users.email] = email
      it[Users.hashedPassword] = hashedPassword
      it[Users.timeZone] = timeZone.toString()
      it[Users.isAdmin] = isAdmin
    }.value

    User(
      id = id,
      name = name,
      email = email,
      hashedPassword = hashedPassword,
      timeZone = timeZone,
      isAdmin = isAdmin
    )
  }

  override suspend fun getUser(id: Int): User? =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Error selecting user from database")) {
      Users.selectAll().where { Users.id eq id }.singleOrNull().let {
        if (it == null) null
        else {
          User(
            id = it[Users.id].value,
            name = it[Users.name],
            email = it[Users.email],
            hashedPassword = it[Users.hashedPassword],
            timeZone = TimeZone.of(it[Users.timeZone]),
            isAdmin = it[Users.isAdmin]
          )
        }
      }
    }

  override suspend fun getUser(email: String): User? =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Error selecting user from database")) {
      Users.selectAll().where { Users.email eq email }.singleOrNull().let {
        if (it == null) null
        else {
          User(
            id = it[Users.id].value,
            name = it[Users.name],
            email = it[Users.email],
            hashedPassword = it[Users.hashedPassword],
            timeZone = TimeZone.of(it[Users.timeZone]),
            isAdmin = it[Users.isAdmin]
          )
        }
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
    timeZone: TimeZone?,
    isAdmin: Boolean?
  ): Boolean = AppDB.safeQuery(onFailureThrowable = DataHandlingError("Error updating user by ID")) {
    Users.update(where = { Users.id eq id }) {
      if (name != null) it[Users.name] = name
      if (email != null) it[Users.email] = email
      if (hashedPassword != null) it[Users.hashedPassword] = hashedPassword
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
      Users.deleteAll() >= 0
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

  override suspend fun getAllPointsByUserId(userId: Int): List<Point> =
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

  override suspend fun createJwtBlackListed(jwt: String, expiresAt: Instant): Unit =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Was not possible to insert JWT in the Black List!")) {
      JwtBlacklist.insert {
        it[JwtBlacklist.jwt] = jwt
        it[JwtBlacklist.expiresAt] = expiresAt
      }
    }

  override suspend fun jwtBlackListContains(jwt: String): Boolean =
    AppDB.safeQuery {
      val now = Clock.System.now()
      val row = JwtBlacklist
        .selectAll()
        .where { JwtBlacklist.jwt eq jwt }
        .singleOrNull()

      if (row != null) {
        val expires = row[JwtBlacklist.expiresAt]

        if (expires <= now) {
          JwtBlacklist.deleteWhere { JwtBlacklist.id eq row[JwtBlacklist.id].value }
          return@safeQuery false
        }

        return@safeQuery true
      }

      false
    }

  override suspend fun clearJwtBlackList(): Unit = AppDB.safeQuery {
    JwtBlacklist.deleteAll()
  }

  suspend fun getLastPointByUserId(userId: Int): Point? =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Error retrieving last point")) {
      Points
        .selectAll()
        .where { Points.relatedUserId eq userId }
        .orderBy(Points.instant to SortOrder.DESC)
        .limit(1)
        .singleOrNull()
        ?.let {
          Point(
            id = it[Points.id].value,
            relatedUserId = it[Points.relatedUserId],
            instant = it[Points.instant]
          )
        }
    }

  suspend fun isUserAdmin(userId: Int): Boolean =
    AppDB.safeQuery(onFailureThrowable = DataHandlingError("Error checking admin privilege")) {
      Users
        .selectAll()
        .where { Users.id eq userId }
        .singleOrNull()
        ?.get(Users.isAdmin)
        ?: false
    }

  suspend fun deleteExpiredBlacklistedTokens() =
    AppDB.safeQuery {
      val now = Clock.System.now()

      JwtBlacklist.deleteWhere {
        JwtBlacklist.expiresAt lessEq now
      }
    }
}
//</editor-fold>

//<editor-fold desc="DATA-USECASES">
object AppUsecases {
  suspend fun signupUser(createUserRequestDTO: CreateUserRequestDTO, isAdmin: Boolean = false): User {
    return ExposedDataCRUD.createUser(
      name = createUserRequestDTO.name,
      email = createUserRequestDTO.email,
      hashedPassword = hashed(createUserRequestDTO.plainPassword),
      timeZone = createUserRequestDTO.timeZone,
      isAdmin = isAdmin
    )
  }

  suspend fun loginUser(credentialsDTO: CredentialsDTO): LoginResponseDTO =
    ExposedDataCRUD.getUser(credentialsDTO.email).let {
      if (it == null || !plainMatchesHashed(credentialsDTO.plainPassword, it.hashedPassword)) {
        throw AuthenticationError("Invalid credentials")
      }

      LoginResponseDTO(
        jwt = JwtGenerator.generate(AppJwtClaims(userId = it.id, isAdmin = it.isAdmin)),
        userDTO = it.toUserDto()
      )
    }

  suspend fun logoutUser(currentUserJwt: String) {
    val expiresAt = extractExpirationFromJwt(currentUserJwt)
    ExposedDataCRUD.createJwtBlackListed(currentUserJwt, expiresAt)
  }

  suspend fun doPoint(userId: Int): Int {
    val user = ExposedDataCRUD.getUser(userId)
      ?: throw AppError("User not found")

    val generatedInstant = Clock.System.now()

    val lastPoint = ExposedDataCRUD.getLastPointByUserId(user.id)

    if (lastPoint != null) {
      val lastInstant = lastPoint.instant
      if (!instantIsAtLeast10SecondsAwayFromLast(check = generatedInstant, lastInstant = lastInstant)) {
        throw RuleViolatedError("Tried to create a point before at least 10 seconds from last point!")
      }
    }

    return ExposedDataCRUD.createPoint(
      relatedUserId = userId,
      instant = generatedInstant
    )
  }

  suspend fun getUserPoints(userId: Int): List<PointDTO> {
    return ExposedDataCRUD.getAllPointsByUserId(userId).map { it.toPointDto() }
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
    suspendTransaction(db = DB) {
      queryCodeBlock()
    }

  /**
   * This is a helper function to automatically tries to perform the
   * query block callback and throw the defined throwable if some error
   * was fired.
   */
  suspend fun <T> safeQuery(
    onFailureThrowable: Throwable? = null,
    queryFunction: suspend () -> T
  ): T = exposedQuery {
    try {
      queryFunction()
    } catch (e: Exception) {
      Constants.logger.error("Database error", e)

      throw (onFailureThrowable ?: DataHandlingError("Database operation failed"))
        .also { it.initCause(e) }
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
      if (jdbcUrl == Constants.DATABASE_SQLITE_URL)
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

  private lateinit var algorithm: Algorithm
  lateinit var verifier: JWTVerifier
    private set

  fun initialize(secret: String) {
    algorithm = Algorithm.HMAC256(secret)
    verifier = JWT.require(algorithm).build()
  }

  fun generate(claims: AppJwtClaims): String {
    return JWT.create()
      .withClaim(AppJwtClaims.USER_ID_KEY, claims.userId)
      .withClaim(AppJwtClaims.IS_ADMIN_KEY, claims.isAdmin)
      .withExpiresAt(claims.expiresAt.toJavaInstant())
      .sign(algorithm)
  }
}

fun ApplicationCall.getAppJwtClaims(): AppJwtClaims? {
  val principal = principal<JWTPrincipal>() ?: return null
  val userId = principal.payload.getClaim(AppJwtClaims.USER_ID_KEY) ?: return null
  val isAdmin = principal.payload.getClaim(AppJwtClaims.IS_ADMIN_KEY) ?: return null
  val expiresAt = principal.payload.expiresAt
  return AppJwtClaims(userId.asInt(), isAdmin.asBoolean(), expiresAt.toInstant().toKotlinInstant())
}

fun ApplicationCall.getBearerToken(): String? {
  return request.headers["Authorization"]
    ?.removePrefix("Bearer ")
    ?.trim()
}

fun extractExpirationFromJwt(token: String): Instant {
  val decoded = JWT.decode(token)
  val exp = decoded.expiresAt ?: throw AuthenticationError("Token without expiration")
  return exp.toInstant().toKotlinInstant()
}
//</editor-fold>

//<editor-fold desc="KTOR-CONFIGURATION-SECTION">
fun Application.authenticationConfiguration() {
  install(Authentication) {
    jwt("flpoint-jwt-auth") {
      verifier(JwtGenerator.verifier)

      validate { jwtCredential ->
        val jwtToken = this.request.headers["Authorization"]
          ?.removePrefix("Bearer ")
          ?.trim()

        // case when token is not present
        if (jwtToken.isNullOrBlank()) {
          return@validate null
        }

        // verifies if the token is in blacklist, if so, don't validate
        if (ExposedDataCRUD.jwtBlackListContains(jwtToken)) {
          return@validate null
        }

        val payload = jwtCredential.payload

        val idClaim = payload.getClaim(AppJwtClaims.USER_ID_KEY)
        val isAdminClaim = payload.getClaim(AppJwtClaims.IS_ADMIN_KEY)

        // explicitly checks claims existence
        if (idClaim.isNull || isAdminClaim.isNull) {
          return@validate null
        }

        val userId = idClaim.asInt()

        // also, as extra, checks "invalid" ID
        if (userId <= 0) {
          return@validate null
        }

        JWTPrincipal(payload)
      }
    }
  }
}

/**
 * Status pages plugin help us to keep track of all error fired in the
 * application flow. This is useful to make the entities of the app
 * throw errors instead returning then. This is nice due we have a
 * limited kind of errors, and they can be caught here, in a centered
 * place.
 *
 * TODO: improve kind of errors and messages.
 */
fun Application.statusPagesConfiguration() {
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      Constants.logger.error("Unhandled exception on ${call.request.uri}", cause)
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
          Constants.logger.trace(cause.message)
          if (cause is BadRequestException) {
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "BadRequestException")
          } else {
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "InternalServerError")
          }
        }
      }
    }
  }
}

suspend fun RoutingContext.handleAsAuthorizedAdmin(
  onSucceedAdminVerification: suspend () -> Unit = {}
) {
  val claims = call.getAppJwtClaims()
    ?: throw AuthenticationError("Invalid JWT claims")

  val isAdmin = ExposedDataCRUD.isUserAdmin(claims.userId)

  if (isAdmin) {
    onSucceedAdminVerification()
  } else {
    throw NoPrivilegeError()
  }
}

fun Application.serializationConfiguration() {
  install(ContentNegotiation) { json(Json { isLenient = false }) }
}

fun Application.configureCORS() {
  install(CORS) {
    allowHeader(HttpHeaders.Authorization)
    allowCredentials = true //may be useful
    allowNonSimpleContentTypes = true //may be useful
    listOf(HttpMethod.Patch, HttpMethod.Get, HttpMethod.Post, HttpMethod.Delete).forEach {
      allowMethod(it)
    }
    anyHost()
  }
}

fun Application.initKtorConfiguration() {
  install(CallLogging) {
    level = Level.INFO
    format { call ->
      val status = call.response.status()
      val httpMethod = call.request.httpMethod.value
      val userAgent = call.request.headers["User-Agent"]
      "Status: $status, HTTP method: $httpMethod, User agent: $userAgent"
    }
  }
  authenticationConfiguration()
  statusPagesConfiguration()
  serializationConfiguration()
  configureCORS()
  routing { routesHandlers() }
}
//</editor-fold>

//<editor-fold desc="KTOR-ROUTES-HANDLERS-SECTION">
fun Routing.routesHandlers() {
  //<editor-fold desc="PUBLIC-ROUTES">
  // global root health route
  get("/health") { call.respondText("Hello from Kotlin/Ktor API!") }

  // used for logging in an existing user
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
    patch("/user/update-password") {
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
    post("/user/point") {
      val claims = call.getAppJwtClaims() ?: throw AppError("Error retrieving JWT claims!")
      val result = AppUsecases.doPoint(claims.userId)
      return@post call.respond(HttpStatusCode.Created, result)
    }

    // TODO: make this cache results
    // Route for user getting only his own points
    get("/user/points") {
      val claims = call.getAppJwtClaims() ?: throw AppError("Error retrieving JWT claims!")
      val result = AppUsecases.getUserPoints(claims.userId)
      return@get call.respond(HttpStatusCode.OK, result)
    }

    post("/user/logout") {
      val jwt = call.getBearerToken()
        ?: throw AuthenticationError("Missing Authorization token")

      AppUsecases.logoutUser(jwt)

      return@post call.respond(HttpStatusCode.OK)
    }
    //</editor-fold>

    //<editor-fold desc="ADMIN-ONLY-ROUTES">
    // admin health route
    get("/admin/health") {
      return@get handleAsAuthorizedAdmin {
        call.respond(HttpStatusCode.OK)
      }
    }

    // used for signup a user
    post("/admin/register") {
      return@post handleAsAuthorizedAdmin {
        val dto = call.receive<CreateUserRequestDTO>()
        val result = AppUsecases.signupUser(dto)
        call.respond(status = HttpStatusCode.Created, message = result)
      }
    }

    // TODO: make this cache results
    // used to get all database users
    get("/admin/users") {
      return@get handleAsAuthorizedAdmin {
        val result = AppUsecases.getAllAppUsers()
        return@handleAsAuthorizedAdmin call.respond(HttpStatusCode.OK, result)
      }
    }

    // used to delete the {id} user
    delete("/admin/users/{id}") {
      return@delete handleAsAuthorizedAdmin {
        val userId = call.parameters["id"] ?: throw AppError("Missing user ID")
        val result = AppUsecases.deleteUser(userId.toInt())
        return@handleAsAuthorizedAdmin call.respond(HttpStatusCode.OK, result)
      }
    }

    // TODO: make this cache results
    // used to retrieve all the points of the database
    get("/admin/points") {
      return@get handleAsAuthorizedAdmin {
        val result = AppUsecases.getAllAppPoints()
        return@handleAsAuthorizedAdmin call.respond(HttpStatusCode.OK, result)
      }
    }
    //</editor-fold>
  }
  //</editor-fold>
}
//</editor-fold>

fun main() {
  val appEnv = System.getenv("APP_ENV")?.lowercase() ?: "dev"
  val isDev = appEnv == "dev"

  Constants.logger.debug("Starting application in environment: $appEnv")

  val jdbcUrl =
    if (isDev) Constants.DATABASE_SQLITE_URL
    else System.getenv("DATABASE_JDBC_URL")
      ?: error("DATABASE_JDBC_URL not defined")

  val jdbcDriver =
    if (isDev) Constants.DATABASE_SQLITE_DRIVER
    else System.getenv("DATABASE_JDBC_CLASS_NAME")
      ?: error("DATABASE_JDBC_CLASS_NAME not defined")

  val dbUser =
    if (isDev) ""
    else System.getenv("DATABASE_USERNAME") ?: ""

  val dbPass =
    if (isDev) ""
    else System.getenv("DATABASE_PASSWORD") ?: ""

  val serverPort =
    if (isDev) 7171
    else System.getenv("WEBSERVER_PORT")?.toIntOrNull()
      ?: error("WEBSERVER_PORT not defined or invalid")

  JwtGenerator.initialize(
    secret = if (isDev) {
      "dev-secret"
    } else {
      System.getenv("JWT_ALGORITHM_SIGN_SECRET")
        ?: error("JWT_ALGORITHM_SIGN_SECRET not defined")
    }
  )

  Constants.logger.info("Connecting using JDBC driver $jdbcDriver")

  AppDB.initialize(
    jdbcUrl = jdbcUrl,
    jdbcDriverClassName = jdbcDriver,
    username = dbUser,
    password = dbPass,
    maximumPoolSize = 5
  ) {
    SchemaUtils.createMissingTablesAndColumns(Users, Points, JwtBlacklist)

    runCatching {
      runBlocking {
        if (isDev) {
          Constants.logger.debug("Creating default DEV users...")

          AppUsecases.signupUser(
            createUserRequestDTO = CreateUserRequestDTO(
              name = "Dev Admin",
              email = "admin@dev.com",
              plainPassword = "123456",
              timeZone = TimeZone.of("America/Sao_Paulo")
            ),
            isAdmin = true
          )

          AppUsecases.signupUser(
            createUserRequestDTO = CreateUserRequestDTO(
              name = "Dev User",
              email = "user@dev.com",
              plainPassword = "123456",
              timeZone = TimeZone.of("America/Sao_Paulo")
            ),
            isAdmin = false
          )
        }

        ExposedDataCRUD.deleteExpiredBlacklistedTokens()
      }
    }.onSuccess {
      Constants.logger.debug("Initial users created (or already existed)")
    }.onFailure {
      Constants.logger.debug("User initialization skipped: ${it.message}")
    }
  }

  embeddedServer(factory = Netty, port = serverPort) {
    initKtorConfiguration()
  }.start(true)
}