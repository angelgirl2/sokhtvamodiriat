package com.angelgirlbrand.modiratsokhtandestelam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inquiry_records")
data class InquiryRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val inquiryType: String, // خلافی راهور، عوارض شهرداری، مالیات نقل و انتقال، عوارض آزادراهی
    val title: String,
    val plateNumber: String,
    val barcodeOrVin: String,
    val nationalId: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val vinCode: String = "",
    val barcode: String = "",
    val engineNumber: String = "",
    val chassisNumber: String = "",
    val postalCode: String = "",
    val address: String = "",
    val amount: Long, // Tomans
    val workflowMethod: String, // "EXPERT_REVIEW" or "DIRECT_PAYMENT"
    val status: String, // "در انتظار بررسی", "تایید شده", "رد شده"
    val transactionRef: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val updatedDateMillis: Long = System.currentTimeMillis()
) {
    val isApproved: Boolean
        get() = status.equals(STATUS_APPROVED, ignoreCase = true) ||
                status.contains("تایید") ||
                status.contains("تسویه") ||
                status.contains("موفق") ||
                status.contains("پرداخت") ||
                status.contains("Approved", ignoreCase = true) ||
                status.contains("OK", ignoreCase = true)

    val isRejected: Boolean
        get() = status.equals(STATUS_REJECTED, ignoreCase = true) ||
                status.contains("رد") ||
                status.contains("ناموفق") ||
                status.contains("Rejected", ignoreCase = true)

    val isPending: Boolean
        get() = !isApproved && !isRejected

    companion object {
        const val STATUS_PENDING = "در انتظار بررسی"
        const val STATUS_APPROVED = "تایید شده"
        const val STATUS_REJECTED = "رد شده"
    }
}
