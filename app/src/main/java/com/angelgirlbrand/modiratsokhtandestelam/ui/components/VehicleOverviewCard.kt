package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceReminderEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.util.PersianDateHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VehicleOverviewCard(
    vehicle: VehicleEntity,
    recentFuelLogs: List<FuelLogEntity>,
    reminders: List<ServiceReminderEntity>,
    onAddFuelClick: () -> Unit,
    onAddReminderClick: () -> Unit,
    onRequestInsuranceClick: () -> Unit,
    onToggleReminderComplete: (ServiceReminderEntity) -> Unit,
    onUpdateInsurance: (expiryMillis: Long, company: String, type: String) -> Unit,
    onDeleteVehicleClick: () -> Unit,
    onEditVehicleClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onNavigateToReminders: (() -> Unit)? = null
) {
    var showEditInsuranceDialog by remember { mutableStateOf(false) }

    // Fuel Status Calculations (Real-time dynamic decrease based on odometer driven distance & increase on refueling)
    val lastFuelLog = remember(recentFuelLogs, vehicle.id) {
        recentFuelLogs.filter { it.vehicleId == vehicle.id }.maxByOrNull { it.dateMillis }
    }

    val (currentFuelLiters, fuelPercentage, estimatedKmRange) = remember(lastFuelLog, vehicle.currentOdometer, vehicle.tankCapacity, vehicle.isMotorcycle) {
        val tankCap = vehicle.tankCapacity.coerceAtLeast(10.0)
        val consumptionPer100Km = if (vehicle.isMotorcycle) 3.0 else 8.2 // L per 100 km average

        if (lastFuelLog == null) {
            val defaultLiters = tankCap * 0.65
            Triple(defaultLiters, 0.65f, (defaultLiters / consumptionPer100Km * 100).toInt())
        } else {
            val baseFuelLiters = if (lastFuelLog.isFullTank) tankCap else lastFuelLog.liters.coerceAtMost(tankCap)
            val drivenKmSinceLog = (vehicle.currentOdometer - lastFuelLog.odometer).coerceAtLeast(0)
            val consumedLiters = drivenKmSinceLog * (consumptionPer100Km / 100.0)
            val remainingLiters = (baseFuelLiters - consumedLiters).coerceIn(0.0, tankCap)
            val pct = (remainingLiters / tankCap).toFloat().coerceIn(0.02f, 1.0f)
            val rangeKm = (remainingLiters / consumptionPer100Km * 100).toInt()
            Triple(remainingLiters, pct, rangeKm)
        }
    }

    val animatedFuelProgress by animateFloatAsState(
        targetValue = fuelPercentage,
        animationSpec = tween(durationMillis = 800),
        label = "fuelProgress"
    )

    // Insurance Expiration Calculations
    val currentTime = System.currentTimeMillis()
    val expiryMillis = vehicle.effectiveInsuranceExpiryMillis
    val daysRemaining = remember(expiryMillis, currentTime) {
        ((expiryMillis - currentTime) / (24 * 3600 * 1000L)).toInt()
    }

    val isInsuranceExpired = daysRemaining <= 0
    val isInsuranceWarning = daysRemaining in 1..30
    val isInsuranceSafe = daysRemaining > 30

    val insuranceStatusColor = when {
        isInsuranceExpired -> Color(0xFFEF4444)
        isInsuranceWarning -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    val insuranceStatusBg = when {
        isInsuranceExpired -> Color(0xFFFEE2E2)
        isInsuranceWarning -> Color(0xFFFEF3C7)
        else -> Color(0xFFD1FAE5)
    }

    val insuranceStatusText = when {
        isInsuranceExpired -> "منقضی شده (${Math.abs(daysRemaining)} روز گذشته)"
        isInsuranceWarning -> "نیاز به تمدید فوری ($daysRemaining روز مانده)"
        else -> "معتبر ($daysRemaining روز باقی‌مانده)"
    }

    // Service Reminders for this vehicle
    val vehicleReminders = remember(reminders, vehicle.id) {
        reminders.filter { it.vehicleId == vehicle.id }
    }
    val activeReminders = remember(vehicleReminders) {
        vehicleReminders.filter { !it.isCompleted }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vehicle_overview_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================= 1. HEADER: Vehicle Identity & Plate =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.primary, Color(0xFF0369A1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = vehicle.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = vehicle.fuelType,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "باک: ${vehicle.tankCapacity.toInt()}L",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onEditVehicleClick != null) {
                        IconButton(
                            onClick = onEditVehicleClick,
                            modifier = Modifier.testTag("edit_vehicle_button")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "ویرایش وسیله نقلیه",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDeleteVehicleClick,
                        modifier = Modifier.testTag("delete_vehicle_button")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "حذف وسیله نقلیه",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Plate & Odometer Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VehiclePlateView(
                    vehicle = vehicle
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "کارکرد فعلی:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "%,d کیلومتر".format(vehicle.currentOdometer),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // ================= 2. ADVANCED FUEL PIE CHART WIDGET =================
            AdvancedFuelPieChartWidget(
                currentLiters = currentFuelLiters,
                tankCapacity = vehicle.tankCapacity,
                fuelPercentage = fuelPercentage,
                estimatedRangeKm = estimatedKmRange,
                lastFuelLog = lastFuelLog,
                onAddFuelClick = onAddFuelClick
            )

            // ================= 3. DEEP DARK THIRD-PARTY INSURANCE CARD =================
            DeepDarkInsuranceCard(
                vehicle = vehicle,
                expiryMillis = expiryMillis,
                daysRemaining = daysRemaining,
                insuranceStatusText = insuranceStatusText,
                insuranceStatusColor = insuranceStatusColor,
                onEditInsuranceClick = { showEditInsuranceDialog = true },
                onRequestInsuranceClick = onRequestInsuranceClick
            )

            // ================= 4. SERVICE REMINDERS SUMMARY =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "یادآورهای سرویس دوره‌ای",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(
                        onClick = onAddReminderClick,
                        modifier = Modifier.testTag("add_reminder_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.AddAlarm, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("افزودن یادآور", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (activeReminders.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "سرویس‌های این خودرو به‌روز است و یادآور فعالی وجود ندارد.",
                            fontSize = 11.sp,
                            color = Color(0xFF047857)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        activeReminders.take(3).forEach { reminder ->
                            val kmRemaining = reminder.targetOdometer - vehicle.currentOdometer
                            val isOverdue = kmRemaining <= 0
                            val isSoon = kmRemaining in 1..1000

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isOverdue) Color(0xFFFEE2E2) else if (isSoon) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Checkbox(
                                            checked = reminder.isCompleted,
                                            onCheckedChange = { onToggleReminderComplete(reminder) },
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = reminder.serviceType,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isOverdue) "موعد سرویس گذشته (%,d کیلومتر پیش)".format(Math.abs(kmRemaining))
                                                else "%,d کیلومتر باقی‌مانده (هدف: %,d)".format(kmRemaining, reminder.targetOdometer),
                                                fontSize = 10.sp,
                                                color = if (isOverdue) Color(0xFFDC2626) else if (isSoon) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isOverdue) Color(0xFFDC2626) else if (isSoon) Color(0xFFD97706) else Color(0xFF0284C7)
                                    ) {
                                        Text(
                                            text = if (isOverdue) "فوری" else if (isSoon) "نزدیک" else "برنامه‌ریزی",
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (activeReminders.size > 3) {
                            Text(
                                text = "+ ${activeReminders.size - 3} یادآور دیگر در تب «یادآورها»",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clickable(enabled = onNavigateToReminders != null) {
                                        onNavigateToReminders?.invoke()
                                    }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Dialog to edit Insurance Expiration ---
    if (showEditInsuranceDialog) {
        EditInsuranceDialog(
            currentCompany = vehicle.insuranceCompany,
            currentType = vehicle.insuranceType,
            currentExpiryMillis = expiryMillis,
            onDismiss = { showEditInsuranceDialog = false },
            onConfirm = { newExpiryMillis, newCompany, newType ->
                onUpdateInsurance(newExpiryMillis, newCompany, newType)
                showEditInsuranceDialog = false
            }
        )
    }
}

@Composable
private fun EditInsuranceDialog(
    currentCompany: String,
    currentType: String,
    currentExpiryMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String) -> Unit
) {
    val companies = listOf("بیمه ایران", "بیمه آسیا", "بیمه دانا", "بیمه البرز", "بیمه پاسارگاد", "بیمه پارسیان", "بیمه کوثر", "بیمه معلم")
    val types = listOf("بیمه شخص ثالث", "بیمه بدنه خودرو", "بیمه حوادث راننده")

    var selectedCompany by remember { mutableStateOf(currentCompany) }
    var selectedType by remember { mutableStateOf(currentType) }
    var validityPeriodMonths by remember { mutableStateOf(12) }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "تنظیم و ویرایش بیمه‌نامه خودرو",
        subtitle = "ثبت تاریخ سررسید و شرکت بیمه‌گر",
        icon = Icons.Default.Security,
        iconTint = Color(0xFF0284C7)
    ) {
        Text("نوع بیمه:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            types.take(2).forEach { t ->
                FilterChip(
                    selected = selectedType == t,
                    onClick = { selectedType = t },
                    label = { Text(t, fontSize = 11.sp) }
                )
            }
        }

        InsuranceAgencyPickerField(
            selectedAgency = selectedCompany,
            onAgencySelected = { selectedCompany = it },
            label = "نمایندگی / شرکت بیمه‌گر:",
            modifier = Modifier.fillMaxWidth()
        )

        Text("مدت اعتبار از امروز:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "۱ ماهه" to 1,
                "۳ ماهه" to 3,
                "۶ ماهه" to 6,
                "۱ ساله" to 12
            ).forEach { (label, months) ->
                FilterChip(
                    selected = validityPeriodMonths == months,
                    onClick = { validityPeriodMonths = months },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val newExpiry = System.currentTimeMillis() + (validityPeriodMonths * 30L * 24 * 3600 * 1000L)
                    onConfirm(newExpiry, selectedCompany, selectedType)
                },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ذخیره و تایید", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("انصراف")
            }
        }
    }
}

// =======================================================================================
// 1. ADVANCED FUEL PIE CHART WIDGET (CANVAS DONUT CHART & HIGH PRECISION NUMERICAL READOUT)
// =======================================================================================
@Composable
fun AdvancedFuelPieChartWidget(
    currentLiters: Double,
    tankCapacity: Double,
    fuelPercentage: Float,
    estimatedRangeKm: Int,
    lastFuelLog: FuelLogEntity?,
    onAddFuelClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = fuelPercentage.coerceIn(0.01f, 1.0f),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "pieProgress"
    )

    // Dynamic Donut colors
    val gaugeColor = when {
        animatedProgress > 0.45f -> Color(0xFF0284C7) // Sky / Ocean Cyan
        animatedProgress > 0.20f -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFFEF4444) // Ruby / Red
    }

    val glowColor = when {
        animatedProgress > 0.45f -> Color(0xFF38BDF8)
        animatedProgress > 0.20f -> Color(0xFFFBBF24)
        else -> Color(0xFFF87171)
    }

    val trackColor = Color(0xFF1E293B)

    Card(
        modifier = Modifier.fillMaxWidth().testTag("advanced_fuel_pie_chart_widget"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.2.dp, gaugeColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = gaugeColor.copy(alpha = 0.25f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = glowColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "ویجت پیشرفته نمودار سوخت (Pie Chart)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = "پایش آنلاین درصد، نوسانات و حجم دقیق باک",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Button(
                    onClick = onAddFuelClick,
                    colors = ButtonDefaults.buttonColors(containerColor = gaugeColor),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("pie_add_fuel_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ثبت سوخت", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            HorizontalDivider(color = Color(0xFF334155))

            // Donut Pie Chart & Numerical Readout Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Jetpack Compose Canvas Donut Pie Chart
                Box(
                    modifier = Modifier.size(105.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 13.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeftOffset = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // 1. Draw Background Capacity Track Arc (Empty portion)
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // 2. Draw Filled Remaining Fuel Arc
                        val sweepAngle = animatedProgress * 360f
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(gaugeColor, glowColor, gaugeColor)
                            ),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Center Pie Readout
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "%.1f٪".format(animatedProgress * 100),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "دقت عددی",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Numerical Metrics Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Metric 1: Remaining Liters
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("حجم سوخت باک:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("%.1f / %.0f L".format(currentLiters, tankCapacity), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = glowColor)
                        }
                    }

                    // Metric 2: Estimated Range
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("شعاع پیمایش:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("$estimatedRangeKm km ⛽", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Metric 3: Fuel Status Indicator Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = gaugeColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, gaugeColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = when {
                                animatedProgress > 0.45f -> "سطح سوخت مطلوب است ✅"
                                animatedProgress > 0.20f -> "سطح سوخت متوسط است ⚠️"
                                else -> "هشدار: وضعیت باک رزرو 🛑"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = glowColor
                        )
                    }
                }
            }
        }
    }
}

// =======================================================================================
// 2. PREMIUM REDESIGNED INSURANCE CARD (GLASSMORPHISM & HOLOGRAPHIC EFFECTS)
// =======================================================================================
@Composable
fun DeepDarkInsuranceCard(
    vehicle: VehicleEntity,
    expiryMillis: Long,
    daysRemaining: Int,
    insuranceStatusText: String,
    insuranceStatusColor: Color,
    onEditInsuranceClick: () -> Unit,
    onRequestInsuranceClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "insurance_glow")
    
    // Smooth glow intensity
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Pulsing radar ring
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("premium_insurance_card")
            .shadow(
                elevation = 15.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = insuranceStatusColor.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)), // Ultra dark
        border = BorderStroke(
            1.5.dp, 
            Brush.sweepGradient(
                colors = listOf(insuranceStatusColor.copy(alpha = 0.8f), Color.Transparent, Color(0xFF38BDF8).copy(alpha = 0.5f), insuranceStatusColor.copy(alpha = 0.8f))
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Advanced Lighting: Top-Down Spotlight
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(insuranceStatusColor.copy(alpha = 0.15f * glowIntensity), Color.Transparent),
                        center = Offset(size.width / 2, -size.height * 0.2f),
                        radius = size.width * 0.8f
                    )
                )
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Top Section: Icon and Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Pulsing Icon Container
                        Box(contentAlignment = Alignment.Center) {
                            // Radar Ring
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                        alpha = pulseAlpha
                                    }
                                    .background(insuranceStatusColor.copy(alpha = 0.4f), CircleShape)
                            )
                            
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = insuranceStatusColor.copy(alpha = 0.2f),
                                border = BorderStroke(1.2.dp, insuranceStatusColor.copy(alpha = 0.4f)),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = insuranceStatusColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                        
                        Column {
                            Text(
                                text = "بیمه‌نامه هوشمند خودرو",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "شرکت: ${vehicle.insuranceCompany.ifBlank { "ثبت‌نشده" }}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Floating Status Badge
                    Surface(
                        shape = CircleShape,
                        color = insuranceStatusColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, insuranceStatusColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = insuranceStatusText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                // Middle Section: Modern Info Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Type Tile
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("نوع پوشش", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = vehicle.insuranceType.ifBlank { "شخص ثالث" },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }

                    // Expiry Tile
                    Surface(
                        modifier = Modifier.weight(1.1f),
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.8.dp, insuranceStatusColor.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("انقضای معتبر", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = PersianDateHelper.toPersianDate(expiryMillis),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                color = insuranceStatusColor
                            )
                        }
                    }
                }

                // Bottom Section: Action Center
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRequestInsuranceClick,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = insuranceStatusColor),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تمدید آنلاین فوری", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }

                    FilledTonalIconButton(
                        onClick = onEditInsuranceClick,
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.06f),
                            contentColor = Color(0xFF38BDF8)
                        )
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
