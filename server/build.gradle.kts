val ktor_version: String by project
val kmongo_version: String by project

plugins {
  kotlin("jvm")
  id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
}

dependencies {
  // dependências do Ktor (core e motor de fundo)
  implementation("io.ktor:ktor-server-core:$ktor_version")
  implementation("io.ktor:ktor-server-netty:$ktor_version")

  // dependências para habilitar serialização
  implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

  // dependências para contentnegotiation para CLIENT
  implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

  // dependência para criptografar a senha
  implementation("org.mindrot:jbcrypt:0.4")

  // dependências para gerenciamento de JWT
  implementation("io.ktor:ktor-server-auth:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")

  // isso aqui serve apenas para gerar os logs da engine do servidor...
  implementation("ch.qos.logback:logback-classic:1.4.8")

  // mongoDB
  implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.10.1")

  // para testes unitários...
  testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
  testImplementation(kotlin("test"))

  // misc, provavelmente melhor lugar para isso é onde contiver clients....
  implementation("io.ktor:ktor-server-cors:$ktor_version")
}