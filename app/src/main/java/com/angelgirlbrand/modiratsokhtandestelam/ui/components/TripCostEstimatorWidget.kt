package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import java.util.Locale

/**
 * Interactive Trip Cost & Fuel Estimator Widget.
 * Calculates needed fuel in liters and exact cost in Tomans based on:
 * - Vehicle's actual fuel consumption per 100km (or factory average)
 * - Fuel type and price per liter (بنزین آزاد ۳۰۰۰ تومان، سهمیه‌ای ۱۵۰۰ تومان، سوپر ۳۵۰۰ تومان، گاز CNG)
 * - User-inputted distance or preset popular route buttons
 * - Round-trip toggle (مسیر رفت و برگشت)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCostEstimatorWidget(
    activeVehicle: VehicleEntity?,
    fuelLogs: List<FuelLogEntity>,
    modifier: Modifier = Modifier
) {
    var distanceInput by remember { mutableStateOf("120") }
    var isRoundTrip by remember { mutableStateOf(false) }
    var selectedFuelRateType by remember { mutableStateOf(1) } // 0 = 1500 (سهمیه‌ای), 1 = 3000 (آزاد), 2 = 3500 (سوپر)
    var customFuelRate by remember { mutableStateOf<Long?>(null) }
    var showAdvancedSettings by remember { mutableStateOf(false) }

    // Calculate actual vehicle average consumption per 100km from previous logs
    val calculatedLitersPer100Km = remember(fuelLogs, activeVehicle?.id) {
        val vehicleLogs = if (activeVehicle != null) fuelLogs.filter { it.vehicleId == activeVehicle.id } else fuelLogs
        if (vehicleLogs.size >= 2) {
            val sorted = vehicleLogs.sortedBy { it.odometer }
            val totalDistance = sorted.last().odometer - sorted.first().odometer
            val totalLiters = sorted.drop(1).sumOf { it.liters }
            if (totalDistance > 0 && totalLiters > 0) {
                (totalLiters / totalDistance) * 100.0
            } else null
        } else null
    }

    // Default based on vehicle type if logs are insufficient
    val defaultConsumptionRate = remember(activeVehicle) {
        if (activeVehicle == null) 7.5
        else if (activeVehicle.isMotorcycle) 2.5
        else when {
            activeVehicle.title.contains("۲۰۶") || activeVehicle.title.contains("206") -> 6.8
            activeVehicle.title.contains("پراید") || activeVehicle.title.contains("131") -> 6.5
            activeVehicle.title.contains("دنا") || activeVehicle.title.contains("سمند") -> 8.2
            activeVehicle.title.contains("تارا") -> 7.4
            else -> 7.5
        }
    }

    var manualConsumptionRate by remember(calculatedLitersPer100Km, defaultConsumptionRate) {
        mutableStateOf(String.format(Locale.US, "%.1f", calculatedLitersPer100Km ?: defaultConsumptionRate))
    }

    val consumptionPer100Km = manualConsumptionRate.toDoubleOrNull() ?: defaultConsumptionRate

    val pricePerLiter: Long = customFuelRate ?: when (selectedFuelRateType) {
        0 -> 1500L  // سهمیه‌ای
        1 -> 3000L  // آزاد
        2 -> 3500L  // سوپر
        else -> 3000L
    }

    val baseDistance = distanceInput.toDoubleOrNull() ?: 0.0
    val effectiveDistance = if (isRoundTrip) baseDistance * 2.0 else baseDistance

    val neededLiters = if (effectiveDistance > 0 && consumptionPer100Km > 0) {
        (effectiveDistance * consumptionPer100Km) / 100.0
    } else 0.0

    val estimatedFuelCost = (neededLiters * pricePerLiter).toLong()

    // Tank capacity comparison
    val tankCapacity = activeVehicle?.tankCapacity ?: 50.0
    val tankFillPercentage = if (tankCapacity > 0) ((neededLiters / tankCapacity) * 100.0).coerceIn(0.0, 100.0) else 0.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trip_cost_estimator_widget"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "تخمین هوشمند هزینه و بنزین سفر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "محاسبه قبل از حرکت بر اساس مصرف ${activeVehicle?.title ?: "خودرو"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = { showAdvancedSettings = !showAdvancedSettings },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (showAdvancedSettings) Icons.Default.ExpandLess else Icons.Default.Tune,
                        contentDescription = "تنظیمات پیشرفته",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Quick preset route buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("۵۰ km (شهری)", "50"),
                    Pair("۱۵۰ km (حومه)", "150"),
                    Pair("۳۵0 km (شمال)", "350"),
                    Pair("۹۰۰ km (مشهد)", "900")
                ).forEach { (label, dist) ->
                    val isSelected = distanceInput == dist
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { distanceInput = dist }
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }

            // Input Row: Distance input + Round trip toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = distanceInput,
                    onValueChange = { distanceInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("مسافت سفر (کیلومتر)") },
                    leadingIcon = {
                        Icon(Icons.Default.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1.3f)
                )

                // Round trip toggle card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isRoundTrip) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isRoundTrip) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(0.9f)
                        .clickable { isRoundTrip = !isRoundTrip }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SyncAlt,
                            contentDescription = null,
                            tint = if (isRoundTrip) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isRoundTrip) "رفت و برگشت ✓" else "فقط یکطرفه",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRoundTrip) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Fuel Rate Selection Tabs (سهمیه‌ای ۱۵۰۰، آزاد ۳۰۰۰، سوپر ۳۵۰۰)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "نرخ بنزین مصرفی:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple(0, "سهمیه‌ای (۱,۵۰۰ ت)", Color(0xFF0D9488)),
                        Triple(1, "آزاد (۳,۰۰۰ ت)", Color(0xFF0284C7)),
                        Triple(2, "سوپر (۳,۵۰۰ ت)", Color(0xFFF59E0B))
                    ).forEach { (typeId, label, color) ->
                        val isSelected = selectedFuelRateType == typeId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFuelRateType = typeId }
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Advanced settings collapse (Custom consumption rate per 100km)
            AnimatedVisibility(visible = showAdvancedSettings) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "تنظیم دقیق مصرف میانگین خودرو:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = manualConsumptionRate,
                                onValueChange = { manualConsumptionRate = it },
                                label = { Text("مصرف هر ۱۰۰ کیلومتر") },
                                trailingIcon = { Text("L/100km", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            if (calculatedLitersPer100Km != null) {
                                Button(
                                    onClick = {
                                        manualConsumptionRate = String.format(Locale.US, "%.1f", calculatedLitersPer100Km)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("از سوابق", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // RESULTS DISPLAY CARD (Prominent Banner)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "بنزین مورد نیاز:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f لیتر", neededLiters),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "هزینه تخمینی سوخت:",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "%,d تومان".format(estimatedFuelCost),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF34D399)
                            )
                        }
                    }

                    // Tank fill progress bar indicator
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "درصد اشغال ظرفیت باک (%.0f لیتری):".format(tankCapacity),
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "%.0f%% ظرفیت باک".format(tankFillPercentage),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tankFillPercentage > 90) Color(0xFFF59E0B) else Color(0xFF38BDF8)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (tankFillPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (tankFillPercentage > 90) Color(0xFFF59E0B) else Color(0xFF10B981),
                            trackColor = Color(0xFF334155),
                        )
                    }

                    // Trip info footer details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "مسافت کل: %.0f کیلومتر %s".format(effectiveDistance, if (isRoundTrip) "(دوطرفه)" else ""),
                            fontSize = 10.5.sp,
                            color = Color(0xFFCBD5E1)
                        )
                        Text(
                            text = "میانگین مصرف: %.1f L/100km".format(consumptionPer100Km),
                            fontSize = 10.5.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }
    }
}
