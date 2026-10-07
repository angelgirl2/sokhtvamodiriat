package com.angelgirlbrand.modiratsokhtandestelam.data.local.dao

import androidx.room.*
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceHistoryDao {
    @Query("SELECT * FROM service_history ORDER BY dateMillis DESC, odometer DESC")
    fun getAllHistory(): Flow<List<ServiceHistoryEntity>>

    @Query("SELECT * FROM service_history WHERE vehicleId = :vehicleId ORDER BY dateMillis DESC, odometer DESC")
    fun getHistoryForVehicle(vehicleId: Long): Flow<List<ServiceHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: ServiceHistoryEntity): Long

    @Update
    suspend fun updateHistory(item: ServiceHistoryEntity)

    @Delete
    suspend fun deleteHistory(item: ServiceHistoryEntity)

    @Query("SELECT SUM(cost) FROM service_history WHERE vehicleId = :vehicleId")
    fun getTotalCostForVehicle(vehicleId: Long): Flow<Long?>
}
