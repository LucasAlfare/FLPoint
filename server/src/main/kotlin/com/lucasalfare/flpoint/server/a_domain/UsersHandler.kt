package com.lucasalfare.flpoint.server.a_domain

import com.lucasalfare.flpoint.server.a_domain.model.User
import com.lucasalfare.flpoint.server.a_domain.model.UserRole

interface UsersHandler {

  suspend fun create(
    name: String,
    email: String,
    hashedPassword: String,
    role: UserRole
  ): Result<Int>

  suspend fun get(id: Int): Result<User>

  suspend fun get(email: String): Result<User>

  suspend fun getAll(): Result<List<User>>

  suspend fun update(
    id: Int,
    name: String? = null,
    email: String? = null,
    hashedPassword: String? = null,
    role: UserRole? = null
  ): Result<Boolean>

  suspend fun remove(id: Int): Result<Boolean>

  suspend fun clear(): Result<Boolean>
}