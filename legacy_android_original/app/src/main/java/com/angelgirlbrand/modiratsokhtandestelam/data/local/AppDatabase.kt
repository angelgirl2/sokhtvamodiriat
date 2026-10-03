package com.angelgirlbrand.modiratsokhtandestelam.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.angelgirlbrand.modiratsokhtandestelam.data.local.dao.*
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.*

@Database(
    entities = [
        VehicleEntity::class,
        FuelLogEntity::class,
        ServiceReminderEntity::class,
        ServiceHistoryEntity::class,
        ServiceRequestEntity::class,
        InquiryRecordEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelLogDao(): FuelLogDao
    abstract fun serviceReminderDao(): ServiceReminderDao
    abstract fun serviceHistoryDao(): ServiceHistoryDao
    abstract fun serviceRequestDao(): ServiceRequestDao
    abstract fun inquiryDao(): InquiryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fuel_and_inquiry_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
