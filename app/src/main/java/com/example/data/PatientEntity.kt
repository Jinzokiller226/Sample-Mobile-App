package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientCode: String,
    val name: String,
    val age: Int,
    val gender: String,
    val phone: String,
    val email: String,
    val address: String,
    val allergies: String,
    val bloodType: String,
    val medicalHistory: String,
    val createdAt: Long = System.currentTimeMillis()
)
