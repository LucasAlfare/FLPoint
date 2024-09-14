@file:Suppress("MemberVisibilityCanBePrivate")

package com.lucasalfare.flpoint.server.b_usecase

import com.lucasalfare.flpoint.server.a_domain.PasswordHasher
import com.lucasalfare.flpoint.server.a_domain.UsersHandler
import com.lucasalfare.flpoint.server.a_domain.model.LoginError
import com.lucasalfare.flpoint.server.a_domain.model.dto.BasicCredentialsDTO
import com.lucasalfare.flpoint.server.a_domain.model.dto.CreateUserDTO
import com.lucasalfare.flpoint.server.c_infra.security.jwt.ktor.KtorJwtGenerator

/**
 * Class that handles the use cases for managing user accounts and authentication.
 *
 * The `UserUsecases` class provides methods to create users and log in users,
 * leveraging user handling and password hashing functionalities.
 *
 * @property usersHandler The handler responsible for performing operations on user records.
 * @property passwordHasher The service used for hashing passwords and verifying hashed passwords.
 */
class UserUsecases(
  var usersHandler: UsersHandler,
  var passwordHasher: PasswordHasher
) {

  /**
   * Creates a new user account based on the provided user data.
   *
   * The method uses the [usersHandler] to create a new user with the specified name, email, hashed password,
   * and target role.
   *
   * @param createUserDTO The data transfer object containing information to create the user, including name, email, plain password, and target role.
   * @return A [Result] containing the ID of the newly created user if successful, or an error otherwise.
   */
  suspend fun createUser(createUserDTO: CreateUserDTO): Result<Int> = usersHandler.create(
    createUserDTO.name,
    createUserDTO.email,
    passwordHasher.hashed(plain = createUserDTO.plainPassword),
    createUserDTO.targetRole
  )

  /**
   * Logs in a user by verifying their credentials and generating a JWT token.
   *
   * The method retrieves the user record based on the provided email, verifies that the plain password matches the
   * hashed password stored in the user record, and generates a JWT token for the authenticated user.
   *
   * @param basicCredentialsDTO The data transfer object containing the email and plain password of the user attempting to log in.
   * @return A JWT token if the login is successful.
   * @throws LoginError If the user is not found or if the password does not match.
   */
  suspend fun loginUser(basicCredentialsDTO: BasicCredentialsDTO): String {
    val result = usersHandler.get(basicCredentialsDTO.email)
//    if (result.isFailure) throw LoginError("")

    val user = result.getOrNull() ?: throw LoginError("User not found for the given email")
    val passwordMatches = passwordHasher.plainMatchesHashed(basicCredentialsDTO.plainPassword, user.hashedPassword)
    if (!passwordMatches) throw LoginError("Invalid password")

    return KtorJwtGenerator.generate(
      id = user.id,
      login = user.email,
      role = user.role
    )
  }
}