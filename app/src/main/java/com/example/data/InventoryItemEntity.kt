package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemCode: String,
    val name: String,
    val category: String,
    val quantity: Int,
    val reorderLevel: Int,
    val unit: String,
    val unitCost: Double,
    val unitPrice: Double,
    val expiryDate: String,
    val supplier: String,
    val updatedAt: Long = System.currentTimeMillis()
)
