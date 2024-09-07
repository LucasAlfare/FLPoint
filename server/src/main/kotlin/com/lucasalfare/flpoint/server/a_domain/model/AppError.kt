package com.lucasalfare.flpoint.server.a_domain.model

class ValidationError : Throwable("Error in validation")
class DatabaseError : Throwable("Error in database operation")
class LoginError : Throwable("Error while trying to log in.")