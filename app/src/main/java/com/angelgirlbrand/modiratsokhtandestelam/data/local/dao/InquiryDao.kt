package com.angelgirlbrand.modiratsokhtandestelam.data.local.dao

import androidx.room.*
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.InquiryRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InquiryDao {
    @Query("SELECT * FROM inquiry_records ORDER BY dateMillis DESC")
    fun getAllInquiries(): Flow<List<InquiryRecordEntity>>

    @Query("SELECT * FROM inquiry_records ORDER BY dateMillis DESC")
    suspend fun getAllInquiriesList(): List<InquiryRecordEntity>

    @Query("SELECT * FROM inquiry_records WHERE id = :id LIMIT 1")
    suspend fun getInquiryById(id: Long): InquiryRecordEntity?

    @Transaction
    @Query("UPDATE inquiry_records SET status = :status, updatedDateMillis = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: InquiryRecordEntity): Long

    @Transaction
    @Update
    suspend fun updateInquiry(inquiry: InquiryRecordEntity)

    @Transaction
    @Delete
    suspend fun deleteInquiry(inquiry: InquiryRecordEntity)
}
