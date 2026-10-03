package com.angelgirlbrand.modiratsokhtandestelam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val plateFirst2: String,
    val plateLetter: String,
    val plateLast3: String,
    val plateCityCode: String,
    val fuelType: String, // بنزین معمولی، بنزین سوپر، گاز سوز CNG، گازوئیل
    val tankCapacity: Double,
    val currentOdometer: Int,
    val insuranceExpiryMillis: Long = 0L, // تاریخ انقضای بیمه‌نامه
    val insuranceCompany: String = "بیمه ایران", // شرکت بیمه‌گر
    val insuranceType: String = "بیمه شخص ثالث", // نوع بیمه
    val inspectionExpiryMillis: Long = 0L, // تاریخ انقضای معاینه فنی
    val inspectionCenter: String = "", // مرکز معاینه فنی
    val inspectionNotifyDaysBefore: Int = 15, // روزهای هشدار قبل از انقضا
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedPlate: String
        get() = "ایران $plateCityCode | $plateLast3 $plateLetter $plateFirst2"

    val effectiveInsuranceExpiryMillis: Long
        get() = if (insuranceExpiryMillis > 0L) {
            insuranceExpiryMillis
        } else {
            createdAt + (365L * 24 * 3600 * 1000)
        }

    val effectiveInspectionExpiryMillis: Long
        get() = if (inspectionExpiryMillis > 0L) {
            inspectionExpiryMillis
        } else {
            // Default 1 year from vehicle creation if not explicitly set
            createdAt + (365L * 24 * 3600 * 1000)
        }
}
