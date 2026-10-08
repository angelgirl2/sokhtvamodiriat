package com.angelgirlbrand.modiratsokhtandestelam.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Only successful messages that were actually accepted by Bale are persisted here.
 * Failed/queued/simulated requests are intentionally not inserted.
 */
@Entity(
    tableName = "bale_request_history",
    indices = [Index(value = ["requestKey"], unique = true)]
)
data class BaleRequestHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val requestKey: String,
    val requestType: String,
    val title: String,
    val summary: String,
    val baleMessageId: String,
    val chatId: String,
    val dateMillis: Long = System.currentTimeMillis()
)
