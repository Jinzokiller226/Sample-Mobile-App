package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "patient_code")
    val patientCode: String,
    val name: String,
    val age: Int,
    val gender: String,
    val phone: String,
    val email: String,
    val address: String,
    val allergies: String,
    @ColumnInfo(name = "blood_type")
    val bloodType: String,
    @ColumnInfo(name = "medical_history")
    val medicalHistory: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
