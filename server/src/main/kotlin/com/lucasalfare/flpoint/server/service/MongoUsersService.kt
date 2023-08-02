package com.lucasalfare.flpoint.server.service

import com.lucasalfare.flpoint.server.model.PointRegistration
import com.lucasalfare.flpoint.server.model.User
import com.lucasalfare.flpoint.server.security.hashPassword
import com.lucasalfare.flpoint.server.service.db.DataCRUDAdapter
import com.lucasalfare.flpoint.server.service.db.mongo.MongoDbSetup
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.Updates.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId


object MongoUsersService : DataCRUDAdapter<User, ObjectId>() {
  private const val COLLECTION_NAME = "users"

  private val usersCollection = MongoDbSetup
    .db
    .getCollection<User>(COLLECTION_NAME)

  override suspend fun create(next: User): Boolean {
    if (getById(next.id!!) != null) return false
    if (next.credentials == null) return false
    if (next.credentials!!.username == null || next.credentials!!.password == null) return false
    if (next.credentials!!.username!!.isEmpty() || next.credentials!!.password!!.isEmpty()) return false
    if (getAll().any { it.credentials!!.username!! == next.credentials!!.username }) return false

    next.credentials!!.hashPassword()

    return usersCollection.insertOne(next).wasAcknowledged()
  }

  override suspend fun getAll() =
    usersCollection.find().toList()

  override suspend fun getById(id: ObjectId) =
    usersCollection.find(eq(User::id.name, id)).firstOrNull()

  override suspend fun removeById(id: ObjectId) =
    usersCollection.deleteOne(eq(User::id.name, id)).wasAcknowledged()

  override suspend fun clear() {
    usersCollection.drop()
  }

  suspend fun createPointRegistration(
    id: ObjectId,
    pointRegistration: PointRegistration?
  ): Boolean {
    if (id.toHexString().isEmpty()) return false
    if (pointRegistration == null) return false
    if (pointRegistration.time == null) return false
    if (!pointRegistration.isValid()) return false

    // TODO: validate by registration limit/frequency rules

    val filter = eq(User::id.name, id)
    val update = combine(
      push(User::pointRegistrations.name, pointRegistration)
    )

    //usersCollection.findOneAndUpdate(filter, update)
    return usersCollection.updateOne(filter, update).wasAcknowledged()
  }
}