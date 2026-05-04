package com.lucasalfare.flpoint.server.application.usecase

import com.lucasalfare.flpoint.server.domain.model.CreateUserRequestDTO
import com.lucasalfare.flpoint.server.domain.model.CredentialsDTO
import com.lucasalfare.flpoint.server.domain.model.LoginResponseDTO
import com.lucasalfare.flpoint.server.domain.model.PointDTO
import com.lucasalfare.flpoint.server.domain.model.UserDTO
import com.lucasalfare.flpoint.server.infrastructure.persistence.ExposedDataCRUD
import com.lucasalfare.flpoint.server.infrastructure.security.JwtGenerator
import com.lucasalfare.flpoint.server.infrastructure.security.AppJwtClaims
import com.lucasalfare.flpoint.server.infrastructure.security.PasswordHashing.hashed
import com.lucasalfare.flpoint.server.infrastructure.security.PasswordHashing.plainMatchesHashed
import com.lucasalfare.flpoint.server.infrastructure.security.JwtUtils.extractExpirationFromJwt
import com.lucasalfare.flpoint.server.domain.validation.validateName
import com.lucasalfare.flpoint.server.domain.validation.validateEmail
import com.lucasalfare.flpoint.server.domain.validation.validatePassword
import com.lucasalfare.flpoint.server.domain.validation.instantIsAtLeast10SecondsAwayFromLast
import com.lucasalfare.flpoint.server.shared.AppError
import com.lucasalfare.flpoint.server.shared.AuthenticationError
import com.lucasalfare.flpoint.server.shared.RuleViolatedError
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

object AppUsecases {
  suspend fun signupUser(createUserRequestDTO: CreateUserRequestDTO, isAdmin: Boolean = false): UserDTO {
    validateName(createUserRequestDTO.name)
    validateEmail(createUserRequestDTO.email)
    validatePassword(createUserRequestDTO.plainPassword)

    val user = ExposedDataCRUD.createUser(
      name = createUserRequestDTO.name,
      email = createUserRequestDTO.email,
      hashedPassword = hashed(createUserRequestDTO.plainPassword),
      timeZone = createUserRequestDTO.timeZone,
      isAdmin = isAdmin
    )
    
    return user.toUserDto()
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
