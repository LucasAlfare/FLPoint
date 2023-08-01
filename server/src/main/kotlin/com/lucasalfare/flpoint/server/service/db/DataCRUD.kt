package com.lucasalfare.flpoint.server.service.db

interface DataCRUD<TItem, TId> {

  suspend fun create(next: TItem): Boolean
  suspend fun getAll(): List<TItem>
  suspend fun getById(id: TId): TItem?
  suspend fun updateById(id: TId, nextValues: TItem): Boolean
  suspend fun removeById(id: TId): Boolean
  suspend fun clear()
}

interface DataCRUDAdapter<TItem, TId> : DataCRUD<TItem, TId> {
  override suspend fun create(next: TItem) = false
  override suspend fun getAll() = emptyList<TItem>()
  override suspend fun getById(id: TId): TItem? = null
  override suspend fun updateById(id: TId, nextValues: TItem) = false
  override suspend fun removeById(id: TId) = false
  override suspend fun clear()
}