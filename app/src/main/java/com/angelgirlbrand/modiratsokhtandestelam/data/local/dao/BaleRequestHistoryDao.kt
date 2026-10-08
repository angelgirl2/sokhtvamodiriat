package com.angelgirlbrand.modiratsokhtandestelam.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.BaleRequestHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BaleRequestHistoryDao {
    @Query("SELECT * FROM bale_request_history ORDER BY dateMillis DESC")
    fun getAllSuccessfulRequests(): Flow<List<BaleRequestHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSuccessfulRequest(record: BaleRequestHistoryEntity): Long

    @Delete
    suspend fun delete(record: BaleRequestHistoryEntity)

    @Query("DELETE FROM bale_request_history")
    suspend fun clearAll()
}
