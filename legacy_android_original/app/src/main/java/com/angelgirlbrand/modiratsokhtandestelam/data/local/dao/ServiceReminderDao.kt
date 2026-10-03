package com.angelgirlbrand.modiratsokhtandestelam.data.local.dao

import androidx.room.*
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceReminderDao {
    @Query("SELECT * FROM service_reminders ORDER BY isCompleted ASC, targetDateMillis ASC")
    fun getAllReminders(): Flow<List<ServiceReminderEntity>>

    @Query("SELECT * FROM service_reminders WHERE vehicleId = :vehicleId ORDER BY targetDateMillis ASC")
    fun getRemindersForVehicle(vehicleId: Long): Flow<List<ServiceReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ServiceReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ServiceReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ServiceReminderEntity)
}
