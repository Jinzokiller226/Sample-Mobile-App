package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "item_code")
    val itemCode: String,
    val name: String,
    val category: String,
    val quantity: Int,
    @ColumnInfo(name = "reorder_level")
    val reorderLevel: Int,
    val unit: String,
    @ColumnInfo(name = "unit_cost")
    val unitCost: Double,
    @ColumnInfo(name = "unit_price")
    val unitPrice: Double,
    @ColumnInfo(name = "expiry_date")
    val expiryDate: String,
    val supplier: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
