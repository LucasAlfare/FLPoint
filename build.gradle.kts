group = "com.lucasalfare.flpoint"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
