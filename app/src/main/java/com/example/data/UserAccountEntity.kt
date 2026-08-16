package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    @ColumnInfo(name = "full_name")
    val fullName: String,
    val role: String = "Staff", // Staff, Doctor, Admin
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
