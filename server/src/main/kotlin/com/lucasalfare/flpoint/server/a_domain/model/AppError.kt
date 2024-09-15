package com.lucasalfare.flpoint.server.a_domain.model


open class HttpError(val code: Int, message: String) : Throwable(message)

class ValidationError(message: String = "Validation Error") : HttpError(422, message)
class DatabaseError(message: String = "Database Error") : HttpError(500, message)
class LoginError(message: String = "Login Error") : HttpError(401, message)
class NoPrivilegeError(message: String = "No Privilege Error") : HttpError(403, message)
class UsecaseRuleError(message: String = "Usecase Rule Error") : HttpError(422, message)
class NullEnvironmentVariableError(message: String = "Null Environment Variable Error") : HttpError(500, message)
class EmptyEnvironmentVariableError(message: String = "Empty Environment Variable Error") : HttpError(500, message)