rootProject.name = "FLPoint"

include(":server")

pluginManagement {
  plugins {
    val kotlinVersion = extra["kotlin_version"] as String

    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
  }
}
