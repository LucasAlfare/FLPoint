plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  application
}

dependencies {
  // Ktor
  implementation(libs.ktor.core)
  implementation(libs.ktor.netty)
  implementation(libs.ktor.content.negotiation)
  implementation(libs.ktor.serialization)
  implementation(libs.ktor.cors)
  implementation(libs.ktor.status.pages)
  implementation(libs.ktor.auth)
  implementation(libs.ktor.auth.jwt)
  implementation(libs.ktor.utils)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.serialization.jackson) // only due to DOCKER bug...

  // Cryptography
  implementation(libs.bcrypt)

  // Exposed (SQL Framework)
  implementation(libs.exposed.core)
  implementation(libs.exposed.jdbc)
  implementation(libs.exposed.kotlin.datetime)

  // Database Drivers
  implementation(libs.jdbc.postgres)
  implementation(libs.jdbc.sqlite) // for easy testing
  implementation(libs.jdbc.h2)

  // HikariCP (Connection Pool)
  implementation(libs.hikaricp)

  // Logging
  implementation(libs.logback)

  // Test
  testImplementation(libs.kotlin.test)
  testImplementation(libs.ktor.test.host)
}

application {
  // Define the main class for the application.
  mainClass.set("com.lucasalfare.flpoint.server.MainKt")
}

tasks.withType<Jar> {
  manifest {
    // "Main-Class" is set to the actual main file path
    attributes["Main-Class"] = application.mainClass
  }

  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  from(configurations.compileClasspath.map { config -> config.map { if (it.isDirectory) it else zipTree(it) } })
}