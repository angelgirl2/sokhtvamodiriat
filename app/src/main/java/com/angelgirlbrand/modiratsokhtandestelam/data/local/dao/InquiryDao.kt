package com.angelgirlbrand.modiratsokhtandestelam.data.local.dao

import androidx.room.*
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.InquiryRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InquiryDao {
    @Query("SELECT * FROM inquiry_records ORDER BY dateMillis DESC")
    fun getAllInquiries(): Flow<List<InquiryRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: InquiryRecordEntity): Long

    @Update
    suspend fun updateInquiry(inquiry: InquiryRecordEntity)

    @Delete
    suspend fun deleteInquiry(inquiry: InquiryRecordEntity)
}
