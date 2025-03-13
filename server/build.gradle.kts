val ktor_version: String by project
val exposed_version: String by project

plugins {
  kotlin("jvm")
  id("org.jetbrains.kotlin.plugin.serialization")
  application
}

dependencies {
  // dependências do Ktor (core e motor de fundo)
  implementation("io.ktor:ktor-server-core:$ktor_version")
  implementation("io.ktor:ktor-server-netty:$ktor_version")

  // dependências para habilitar serialização
  implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

  // status pages
  implementation("io.ktor:ktor-server-status-pages:$ktor_version")

  // CORS
  implementation("io.ktor:ktor-server-cors:$ktor_version")

  // dependências para gerenciamento de JWT
  implementation("io.ktor:ktor-server-auth:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")

  // For some reason, above JWT auth not works without this :/
  // this happens only in Docker!! :(
  implementation("io.ktor:ktor-serialization-jackson:$ktor_version")

  // dependências para obter auxiliares de datas
  implementation("io.ktor:ktor-utils:$ktor_version")

  // dependências para contentnegotiation para CLIENT
  implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

  // dependência para criptografar a senha
  implementation("org.mindrot:jbcrypt:0.4")

  // Dependencies for database manipulation
  implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposed_version")

  /*
  Database.connect("jdbc:sqlite:/data/data.db", "org.sqlite.JDBC")
  TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
   */
  implementation("org.xerial:sqlite-jdbc:3.45.2.0")

  /*
  Database.connect("jdbc:h2:mem:regular", "org.h2.Driver")
   */
  implementation("com.h2database:h2:2.3.232")

  implementation("com.zaxxer:HikariCP:5.1.0")

  // isso aqui serve apenas para gerar os logs da engine do servidor...
  implementation("ch.qos.logback:logback-classic:1.4.14")

  // para testes unitários...
  testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
  testImplementation(kotlin("test"))
}

kotlin {
  jvmToolchain(23)
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