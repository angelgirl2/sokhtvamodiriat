package com.angelgirlbrand.modiratsokhtandestelam.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        InquiryRecordEntity::class,
        BaleRequestHistoryEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelLogDao(): FuelLogDao
    abstract fun serviceReminderDao(): ServiceReminderDao
    abstract fun serviceHistoryDao(): ServiceHistoryDao
    abstract fun serviceRequestDao(): ServiceRequestDao
    abstract fun inquiryDao(): InquiryDao
    abstract fun baleRequestHistoryDao(): BaleRequestHistoryDao

    companion object {
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bale_request_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        requestKey TEXT NOT NULL,
                        requestType TEXT NOT NULL,
                        title TEXT NOT NULL,
                        summary TEXT NOT NULL,
                        baleMessageId TEXT NOT NULL,
                        chatId TEXT NOT NULL,
                        dateMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_bale_request_history_requestKey ON bale_request_history(requestKey)"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fuel_and_inquiry_db"
                )
                    .addMigrations(MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
