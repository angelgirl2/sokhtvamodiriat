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
    val status: String, // "در انتظار پرداخت", "در حال بررسی توسط کارشناس", "پرداخت شد و تسویه گردید"
    val transactionRef: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)
