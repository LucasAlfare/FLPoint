package com.lucasalfare.flpoint.server.a_domain.model

/**
 * Exception representing an error in validation processes.
 *
 * Typically thrown when validation of input data fails, such as invalid formats or constraints.
 */
class ValidationError : Throwable("Error in validation")

/**
 * Exception representing an error related to database operations.
 *
 * This may be thrown when a database operation fails, such as connection issues or query failures.
 */
class DatabaseError : Throwable("Error in database operation")

/**
 * Exception representing an error during login attempts.
 *
 * Thrown when a user fails to log in, usually due to incorrect credentials or other login issues.
 */
class LoginError : Throwable("Error while trying to log in.")

/**
 * Exception representing a lack of privileges for accessing a route.
 *
 * Thrown when a user attempts to access a resource or route they do not have permission for.
 */
class NoPrivilegeError : Throwable("No privileges to access the desired route.")

/**
 * Exception representing a violation of a server-side rule in use case logic.
 *
 * Thrown when a business rule or server constraint is violated during the execution of a use case.
 */
class UsecaseRuleError : Throwable("A server rule was not respected.")