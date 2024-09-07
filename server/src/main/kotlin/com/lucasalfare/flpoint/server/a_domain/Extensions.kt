package com.lucasalfare.flpoint.server.a_domain

fun Throwable.customRootCause(): Throwable =
  if (cause == null) this else cause!!.customRootCause()