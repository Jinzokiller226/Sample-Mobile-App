package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visit_records")
data class VisitRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val patientName: String,
    val visitDate: String,
    val diagnosis: String,
    val prescription: String,
    val doctorNotes: String,
    val cost: Double
)
