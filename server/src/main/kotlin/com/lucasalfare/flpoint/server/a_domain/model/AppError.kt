package com.lucasalfare.flpoint.server.a_domain.model

class ValidationError : Throwable("Error in validation")
class DatabaseError : Throwable("Error in database operation")
class LoginError : Throwable("Error while trying to log in.")
class NoPrivilegeError : Throwable("No privileges to access the desired route.")
class UsecaseRuleError : Throwable("A server rule was not respected.")