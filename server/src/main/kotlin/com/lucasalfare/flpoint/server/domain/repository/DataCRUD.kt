package com.lucasalfare.flpoint.server.domain.repository

import com.lucasalfare.flpoint.server.domain.model.User
import com.lucasalfare.flpoint.server.domain.model.Point
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

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
