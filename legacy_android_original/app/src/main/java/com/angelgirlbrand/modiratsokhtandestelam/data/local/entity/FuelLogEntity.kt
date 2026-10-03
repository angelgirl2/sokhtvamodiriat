package com.angelgirlbrand.modiratsokhtandestelam.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fuel_logs",
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
data class FuelLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val dateMillis: Long,
    val odometer: Int,
    val liters: Double,
    val pricePerLiter: Long, // Tomans
    val totalCost: Long, // Tomans
    val stationName: String,
    val isFullTank: Boolean = true,
    val notes: String = ""
)
