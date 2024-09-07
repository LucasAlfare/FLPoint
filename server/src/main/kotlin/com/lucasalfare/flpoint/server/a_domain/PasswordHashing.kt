package com.lucasalfare.flpoint.server.a_domain

interface PasswordHashing {

  fun hashed(plain: String): String

  fun plainMatchHashed(plain: String, hashed: String): Boolean
}