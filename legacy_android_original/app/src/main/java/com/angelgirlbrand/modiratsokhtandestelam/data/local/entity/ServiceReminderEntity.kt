package com.angelgirlbrand.modiratsokhtandestelam.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_reminders",
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
data class ServiceReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val serviceType: String, // روغن موتور، لنت، تسمه تایم، فیلتر و ...
    val targetDateMillis: Long,
    val targetOdometer: Int,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val notified: Boolean = false
)
