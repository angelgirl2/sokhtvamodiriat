package com.angelgirlbrand.modiratsokhtandestelam.data.local.dao

import androidx.room.*
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY createdAt DESC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId LIMIT 1")
    suspend fun getVehicleById(vehicleId: Long): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun getVehicleCount(): Int
}
