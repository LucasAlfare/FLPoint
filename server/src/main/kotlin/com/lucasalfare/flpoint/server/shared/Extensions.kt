package com.lucasalfare.flpoint.server.shared

fun Throwable.customRootCause(): Throwable {
  val visited = mutableSetOf<Throwable>()
  var current = this

  while (current.cause != null && current.cause !in visited) {
    visited += current
    current = current.cause!!
  }

  return current
}
