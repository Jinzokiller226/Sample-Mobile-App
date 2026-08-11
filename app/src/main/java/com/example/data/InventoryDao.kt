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

    @Query("SELECT * FROM inventory_items WHERE quantity <= reorderLevel ORDER BY quantity ASC")
    fun getLowStockItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%' OR itemCode LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchInventory(query: String): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET quantity = :newQty, updatedAt = :time WHERE id = :id")
    suspend fun updateQuantity(id: Long, newQty: Int, time: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM inventory_items")
    fun getInventoryCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM inventory_items WHERE quantity <= reorderLevel")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT * FROM inventory_items")
    suspend fun getAllInventorySnapshot(): List<InventoryItemEntity>
}
