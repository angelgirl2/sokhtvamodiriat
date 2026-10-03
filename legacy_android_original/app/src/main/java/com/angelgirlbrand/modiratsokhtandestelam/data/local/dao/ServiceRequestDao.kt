package com.angelgirlbrand.modiratsokhtandestelam.data.local.dao

import androidx.room.*
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRequestDao {
    @Query("SELECT * FROM service_requests ORDER BY submissionDateMillis DESC")
    fun getAllRequests(): Flow<List<ServiceRequestEntity>>

    @Query("SELECT * FROM service_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: Long): ServiceRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ServiceRequestEntity): Long

    @Update
    suspend fun updateRequest(request: ServiceRequestEntity)

    @Delete
    suspend fun deleteRequest(request: ServiceRequestEntity)
}
