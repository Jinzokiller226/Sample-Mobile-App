package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllInventory(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%' OR item_code LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchInventory(query: String): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventoryItemEntity): Long

    @Update
    suspend fun update(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET quantity = :newQuantity, updated_at = :timestamp WHERE id = :id")
    suspend fun updateStock(id: Long, newQuantity: Int, timestamp: Long)

    @Delete
    suspend fun delete(item: InventoryItemEntity)
}
