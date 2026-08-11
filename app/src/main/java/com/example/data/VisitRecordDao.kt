package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitRecordDao {
    @Query("SELECT * FROM visit_records ORDER BY id DESC")
    fun getAllVisits(): Flow<List<VisitRecordEntity>>

    @Query("SELECT * FROM visit_records WHERE patientId = :patientId ORDER BY id DESC")
    fun getVisitsForPatient(patientId: Long): Flow<List<VisitRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitRecordEntity): Long

    @Query("SELECT COUNT(*) FROM visit_records")
    fun getVisitCount(): Flow<Int>

    @Query("SELECT * FROM visit_records")
    suspend fun getAllVisitsSnapshot(): List<VisitRecordEntity>
}
