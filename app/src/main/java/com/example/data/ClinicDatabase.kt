package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PatientEntity::class, InventoryItemEntity::class, VisitRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ClinicDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun visitRecordDao(): VisitRecordDao

    companion object {
        @Volatile
        private var INSTANCE: ClinicDatabase? = null

        fun getDatabase(context: Context): ClinicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClinicDatabase::class.java,
                    "clinic_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
