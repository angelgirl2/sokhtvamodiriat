package com.angelgirlbrand.modiratsokhtandestelam.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_history",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vehicleId"])]
)
data class ServiceHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val serviceType: String, // تعویض روغن موتور، فیلتر روغن، فیلتر هوا، فیلتر بنزین، لنت ترمز، تسمه تایم و ...
    val itemsChanged: String, // شرح قطعات و اقلام تعویض شده
    val odometer: Int, // کیلومتر خودرو هنگام سرویس
    val nextDueOdometer: Int = 0, // کیلومتر پیشنهادی برای سرویس بعدی
    val dateMillis: Long, // تاریخ انجام سرویس
    val cost: Long, // هزینه به تومان
    val mechanicOrShop: String = "", // نام تعمیرگاه یا تعویض روغنی
    val notes: String = "" // توضیحات یا برند قطعات
)
