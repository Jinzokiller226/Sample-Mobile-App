package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    @Query("SELECT * FROM visit_records ORDER BY visit_date DESC")
    fun getAllVisits(): Flow<List<VisitRecordEntity>>

    @Query("SELECT * FROM visit_records WHERE patient_id = :patientId ORDER BY visit_date DESC")
    fun getVisitsForPatient(patientId: Long): Flow<List<VisitRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(visit: VisitRecordEntity): Long
}
