package com.lucasalfare.flpoint.server.data.services.validators

interface Validator {

  suspend fun isValid(): Boolean
}