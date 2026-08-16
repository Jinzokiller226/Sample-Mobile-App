package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visit_records")
data class VisitRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "patient_id")
    val patientId: Long,
    @ColumnInfo(name = "patient_name")
    val patientName: String,
    @ColumnInfo(name = "visit_date")
    val visitDate: String,
    val diagnosis: String,
    val prescription: String,
    @ColumnInfo(name = "doctor_notes")
    val doctorNotes: String,
    val cost: Double
)
