package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceReminderEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
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
    modifier: Modifier = Modifier,
    onNavigateToReminders: (() -> Unit)? = null
) {
    var showEditInsuranceDialog by remember { mutableStateOf(false) }

    // Fuel Status Calculations
    val lastFuelLog = remember(recentFuelLogs, vehicle.id) {
        recentFuelLogs.filter { it.vehicleId == vehicle.id }.maxByOrNull { it.dateMillis }
    }

    val fuelPercentage = remember(lastFuelLog, vehicle.tankCapacity) {
        if (lastFuelLog == null) {
            0.5f // Default indication when no log yet
        } else {
            val estimated = if (lastFuelLog.isFullTank) {
                1.0f
            } else {
                (lastFuelLog.liters / vehicle.tankCapacity.coerceAtLeast(10.0)).toFloat().coerceIn(0.1f, 1.0f)
            }
            estimated
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

                IconButton(
                    onClick = onDeleteVehicleClick,
                    modifier = Modifier.testTag("delete_vehicle_button")
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "حذف خودرو",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
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
                IranianPlateView(
                    first2 = vehicle.plateFirst2,
                    letter = vehicle.plateLetter,
                    last3 = vehicle.plateLast3,
                    cityCode = vehicle.plateCityCode
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

            // ================= 2. FUEL STATUS SUMMARY =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0284C7).copy(alpha = 0.07f))
                    .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.2f), RoundedCornerShape(14.dp))
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
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "وضعیت سوخت خودرو",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(
                        onClick = onAddFuelClick,
                        modifier = Modifier.testTag("add_fuel_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("ثبت سوخت", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Fuel Gauge Bar
                val fuelGaugeColor = when {
                    animatedFuelProgress > 0.45f -> Color(0xFF0284C7)
                    animatedFuelProgress > 0.20f -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (lastFuelLog?.isFullTank == true) "باک پر (۱۰۰٪)"
                            else "تخمین سوخت: ${(animatedFuelProgress * 100).toInt()}٪",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = fuelGaugeColor
                        )
                        Text(
                            text = "ظرفیت: ${vehicle.tankCapacity.toInt()} لیتر",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LinearProgressIndicator(
                        progress = { animatedFuelProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = fuelGaugeColor,
                        trackColor = Color(0xFFE2E8F0)
                    )
                }

                if (lastFuelLog != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "آخرین سوخت: %.1fL در %s".format(
                                lastFuelLog.liters,
                                dateFormat.format(Date(lastFuelLog.dateMillis))
                            ),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (lastFuelLog.stationName.isNotBlank()) {
                            Text(
                                text = lastFuelLog.stationName,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    Text(
                        text = "هنوز سابقه سوخت‌گیری ثبت نشده است؛ با لمس «ثبت سوخت» شروع کنید.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ================= 3. INSURANCE EXPIRATION SUMMARY =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(insuranceStatusBg.copy(alpha = 0.45f))
                    .border(1.dp, insuranceStatusColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
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
                                .background(insuranceStatusColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isInsuranceExpired) Icons.Default.Warning else Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = vehicle.insuranceType,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "شرکت صادرکننده: ${vehicle.insuranceCompany}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = insuranceStatusColor
                    ) {
                        Text(
                            text = insuranceStatusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سررسید: ${dateFormat.format(Date(expiryMillis))}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { showEditInsuranceDialog = true },
                            modifier = Modifier.testTag("edit_insurance_button"),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("ویرایش تاریخ", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onRequestInsuranceClick,
                            modifier = Modifier.testTag("renew_insurance_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = insuranceStatusColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("درخواست تمدید بیمه", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "تنظیم و ویرایش بیمه‌نامه خودرو",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

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

                Text("شرکت بیمه‌گر:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        companies.take(3).forEach { c ->
                            FilterChip(
                                selected = selectedCompany == c,
                                onClick = { selectedCompany = c },
                                label = { Text(c, fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        companies.drop(3).take(3).forEach { c ->
                            FilterChip(
                                selected = selectedCompany == c,
                                onClick = { selectedCompany = c },
                                label = { Text(c, fontSize = 11.sp) }
                            )
                        }
                    }
                }

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

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val newExpiry = System.currentTimeMillis() + (validityPeriodMonths * 30L * 24 * 3600 * 1000L)
                            onConfirm(newExpiry, selectedCompany, selectedType)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ذخیره و تایید")
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("انصراف")
                    }
                }
            }
        }
    }
}
