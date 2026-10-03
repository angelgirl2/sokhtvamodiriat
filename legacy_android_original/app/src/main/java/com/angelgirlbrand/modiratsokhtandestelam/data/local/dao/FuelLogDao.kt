package com.angelgirlbrand.modiratsokhtandestelam.data.local.dao

import androidx.room.*
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelLogDao {
    @Query("SELECT * FROM fuel_logs ORDER BY dateMillis DESC")
    fun getAllLogs(): Flow<List<FuelLogEntity>>

    @Query("SELECT * FROM fuel_logs WHERE vehicleId = :vehicleId ORDER BY dateMillis DESC")
    fun getLogsForVehicle(vehicleId: Long): Flow<List<FuelLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FuelLogEntity): Long

    @Update
    suspend fun updateLog(log: FuelLogEntity)

    @Delete
    suspend fun deleteLog(log: FuelLogEntity)

    @Query("SELECT SUM(totalCost) FROM fuel_logs")
    fun getTotalFuelCost(): Flow<Long?>

    @Query("SELECT SUM(liters) FROM fuel_logs")
    fun getTotalFuelLiters(): Flow<Double?>
}
