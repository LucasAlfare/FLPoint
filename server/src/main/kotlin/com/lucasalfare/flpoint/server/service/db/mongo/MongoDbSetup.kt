package com.lucasalfare.flpoint.server.service.db.mongo

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase

object MongoDbSetup {
  private const val DATABASE_NAME = "FLPoint"
  private val mongoPassword = System.getenv("MONGODB_PASSWORD")
  private val url =
    "mongodb+srv://lucasalfare:${mongoPassword}@flpointcluster1.8q0jjmy.mongodb.net/?retryWrites=true&w=majority"

  val db: MongoDatabase
    get() = MongoClient
      .create(url)
      .getDatabase(DATABASE_NAME)
}
