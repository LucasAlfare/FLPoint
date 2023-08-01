package com.lucasalfare.flpoint.server.service

import com.lucasalfare.flpoint.server.model.User
import com.lucasalfare.flpoint.server.service.db.DataCRUDAdapter
import com.lucasalfare.flpoint.server.service.db.mongo.MongoDbSetup
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId


object MongoUsersService : DataCRUDAdapter<User, ObjectId> {
  private const val COLLECTION_NAME = "users"

  private val usersCollection = MongoDbSetup
    .db
    .getCollection<User>(COLLECTION_NAME)

  override suspend fun create(next: User): Boolean {
    // TODO: validate user before insert
    val result = usersCollection.insertOne(next).wasAcknowledged()
    return result
  }

  override suspend fun getAll() =
    usersCollection.find().toList()

  override suspend fun getById(id: ObjectId) =
    usersCollection.find(eq(User::id.name, id)).firstOrNull()

  override suspend fun updateById(id: ObjectId, nextValues: User): Boolean {
    val filter = eq(User::id.name, id)
    val update = combine(
      set(User::maxAuthenticationsPerDay.name, nextValues.maxAuthenticationsPerDay)
    )

    return usersCollection.updateOne(filter, update).wasAcknowledged()
  }

  override suspend fun removeById(id: ObjectId) =
    usersCollection.deleteOne(eq(User::id.name, id)).wasAcknowledged()

  override suspend fun clear() {
    usersCollection.drop()
  }
}