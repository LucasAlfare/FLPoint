package com.lucasalfare.flpoint.server.a_domain

/**
 * Extension function for `Throwable` to recursively retrieve the root cause of an exception.
 *
 * This function traverses the chain of causes to find the original (root) exception that triggered the current exception.
 *
 * @return The root cause of the exception. If the current throwable has no cause, it returns itself.
 */
fun Throwable.customRootCause(): Throwable =
  if (cause == null) this else cause!!.customRootCause()