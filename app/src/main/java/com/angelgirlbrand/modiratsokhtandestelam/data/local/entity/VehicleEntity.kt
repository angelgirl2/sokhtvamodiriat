package com.angelgirlbrand.modiratsokhtandestelam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val plateFirst2: String, // خودرو: ۲ رقم اول | موتور: ۳ رقم بالا
    val plateLetter: String, // خودرو: حرف میانی | موتور: "موتور" یا خالی
    val plateLast3: String,  // خودرو: ۳ رقم دوم | موتور: ۵ رقم پایین
    val plateCityCode: String, // خودرو: کد ایران (مثلا ۲۱) | موتور: کد شهر یا خالی
    val fuelType: String, // بنزین معمولی، بنزین سوپر، گاز سوز CNG، گازوئیل
    val tankCapacity: Double,
    val currentOdometer: Int,
    val vehicleType: String = TYPE_CAR, // "خودرو"، "اروندی"، "موتورسیکلت"
    val insuranceExpiryMillis: Long = 0L,
    val insuranceCompany: String = "بیمه ایران",
    val insuranceType: String = "بیمه شخص ثالث",
    val inspectionExpiryMillis: Long = 0L,
    val inspectionCenter: String = "",
    val inspectionNotifyDaysBefore: Int = 15,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_CAR = "خودرو"
        const val TYPE_MOTORCYCLE = "موتورسیکلت"
        const val TYPE_ARVAND = "اروندی"
    }

    val isMotorcycle: Boolean
        get() = vehicleType == TYPE_MOTORCYCLE || plateLetter == "موتور"

    val isArvand: Boolean
        get() = vehicleType == TYPE_ARVAND || plateLetter == "اروند"

    val formattedPlate: String
        get() = when {
            isMotorcycle -> "موتور $plateFirst2 - $plateLast3"
            isArvand -> "اروند ${plateLast3.ifEmpty { plateFirst2 }}"
            else -> "ایران $plateCityCode | $plateLast3 $plateLetter $plateFirst2"
        }

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
            createdAt + (365L * 24 * 3600 * 1000)
        }
}
