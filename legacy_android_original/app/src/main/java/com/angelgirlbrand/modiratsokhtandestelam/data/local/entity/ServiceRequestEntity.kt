package com.angelgirlbrand.modiratsokhtandestelam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_requests")
data class ServiceRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val requestType: String, // بیمه، استعلام خلافی، عوارض آزادراهی، عوارض سالیانه، مالیات نقل و انتقال، درخواست کارت سوخت
    val title: String,
    val fullName: String,
    val nationalCode: String,
    val phoneNumber: String,
    val vehiclePlate: String,
    val vinCode: String = "",
    val barcodeNumber: String = "",
    val engineNumber: String = "",
    val chassisNumber: String = "",
    val postalCode: String = "",
    val address: String = "",
    val insuranceCategory: String = "", // دسته‌بندی بیمه: شخص ثالث، بدنه، حوادث راننده، موتور سیکلت
    val insuranceCompany: String = "", // شرکت بیمه‌گر
    val durationMonths: Int = 12,
    val discountPercent: Int = 0,
    val additionalDetails: String = "",
    val status: String = STATUS_PENDING,
    val baleMessageId: String = "",
    val submissionDateMillis: Long = System.currentTimeMillis(),
    val updatedDateMillis: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "در انتظار تایید مدیر"
        const val STATUS_APPROVED = "تایید شد و برای شما اطلاعات ارسال میگردد"
        const val STATUS_REJECTED = "رد شده - نیاز به بررسی مجدد"
    }
}
