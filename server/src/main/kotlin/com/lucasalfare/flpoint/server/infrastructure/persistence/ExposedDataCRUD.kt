package com.lucasalfare.flpoint.server.infrastructure.persistence

import com.lucasalfare.flpoint.server.domain.repository.DataCRUD
import com.lucasalfare.flpoint.server.domain.model.User
import com.lucasalfare.flpoint.server.domain.model.Point
import com.lucasalfare.flpoint.server.shared.DataHandlingError
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.Instant

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
