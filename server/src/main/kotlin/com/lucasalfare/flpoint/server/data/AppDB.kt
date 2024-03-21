@file:Suppress("SameParameterValue")

package com.lucasalfare.flpoint.server.data

import com.lucasalfare.flpoint.server.data.tables.JustificationsTable
import com.lucasalfare.flpoint.server.data.tables.TimeRegistrationsTable
import com.lucasalfare.flpoint.server.data.tables.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object AppDB {

  private lateinit var hikariDataSource: HikariDataSource

  fun initialize(
    username: String,
    password: String
  ) {
    hikariDataSource = createHikariDataSource(
      jdbcUrl = "jdbc:sqlite:./data/data.db",
      username = username,
      password = password
    )

    transaction(Database.connect(hikariDataSource)) {
      SchemaUtils.createMissingTablesAndColumns(UsersTable, TimeRegistrationsTable, JustificationsTable)
    }
  }

  suspend fun <T> query(block: suspend () -> T): T =
    newSuspendedTransaction(
      context = Dispatchers.IO,
      db = Database.connect(hikariDataSource)
    ) {
      block()
    }

  private fun createHikariDataSource(
    jdbcUrl: String,
    username: String,
    password: String
  ): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
      this.jdbcUrl = jdbcUrl
      this.driverClassName = "org.sqlite.JDBC" // TODO: switch to targeted driver
      this.username = username
      this.password = password
      this.maximumPoolSize = 20
      this.isAutoCommit = true
      this.transactionIsolation = IsolationLevel.TRANSACTION_READ_COMMITTED.name
      this.validate()
    }

    return HikariDataSource(hikariConfig)
  }
}