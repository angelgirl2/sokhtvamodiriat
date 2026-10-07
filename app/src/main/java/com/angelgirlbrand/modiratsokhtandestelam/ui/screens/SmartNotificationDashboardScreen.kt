package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel

/**
 * SmartNotificationDashboardScreen:
 * A dedicated, highly functional dashboard displaying intelligent notification alerts for fuel, insurance, and service maintenance.
 * Features full notification customization controls (alert thresholds, toggles, sound/vibration choices) saved locally.
 */
@Composable
fun SmartNotificationDashboardScreen(
    fuelViewModel: FuelViewModel,
    baleViewModel: BaleServiceViewModel,
    onNavigateToInsurance: () -> Unit,
    onNavigateToFuel: () -> Unit,
    onNavigateToInquiry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehicles by fuelViewModel.vehicles.collectAsState()
    val selectedVehicleId by fuelViewModel.selectedVehicleId.collectAsState()
    val activeVehicle = remember(vehicles, selectedVehicleId) {
        vehicles.firstOrNull { it.id == selectedVehicleId } ?: vehicles.firstOrNull()
    }

    // Notification Customization Preferences (SharedPreferences)
    val prefs = remember { context.getSharedPreferences("smart_notifications_pref_v1", Context.MODE_PRIVATE) }

    var isMasterAlertsEnabled by remember { mutableStateOf(prefs.getBoolean("master_alerts", true)) }
    var isInsuranceAlertEnabled by remember { mutableStateOf(prefs.getBoolean("insurance_alerts", true)) }
    var insuranceAlertDaysThreshold by remember { mutableIntStateOf(prefs.getInt("insurance_days_threshold", 15)) }

    var isServiceAlertEnabled by remember { mutableStateOf(prefs.getBoolean("service_alerts", true)) }
    var serviceAlertKmThreshold by remember { mutableIntStateOf(prefs.getInt("service_km_threshold", 500)) }

    var isFuelQuotaAlertEnabled by remember { mutableStateOf(prefs.getBoolean("fuel_quota_alerts", true)) }
    var fuelQuotaPercentThreshold by remember { mutableIntStateOf(prefs.getInt("fuel_quota_percent_threshold", 20)) }

    var isSoundVibrationEnabled by remember { mutableStateOf(prefs.getBoolean("sound_vibration", true)) }
    var isFloatingBannerEnabled by remember { mutableStateOf(prefs.getBoolean("floating_banner", true)) }

    val saveNotificationSettings = {
        prefs.edit().apply {
            putBoolean("master_alerts", isMasterAlertsEnabled)
            putBoolean("insurance_alerts", isInsuranceAlertEnabled)
            putInt("insurance_days_threshold", insuranceAlertDaysThreshold)
            putBoolean("service_alerts", isServiceAlertEnabled)
            putInt("service_km_threshold", serviceAlertKmThreshold)
            putBoolean("fuel_quota_alerts", isFuelQuotaAlertEnabled)
            putInt("fuel_quota_percent_threshold", fuelQuotaPercentThreshold)
            putBoolean("sound_vibration", isSoundVibrationEnabled)
            putBoolean("floating_banner", isFloatingBannerEnabled)
            apply()
        }
        com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("⚙️ تنظیمات شخصی‌سازی اعلان‌ها با موفقیت ذخیره گردید")
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // =====================================================================
        // 1. DASHBOARD HERO HEADER BANNER
        // =====================================================================
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(animationSpec = tween(500), initialOffsetY = { 40 })
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("smart_alerts_hero_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF0284C7),
                                    Color(0xFF0369A1)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF38BDF8).copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "سیستم پایش و هشدارهای هوشمند ⚡",
                                        color = Color(0xFFE0F2FE),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Text(
                            text = "داشبورد اطلاعیه‌های هوشمند سوخت، بیمه و سرویس",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "مشاهده زنده هشدارهای انقضای بیمه‌نامه، سرویس‌های دوره‌ای روغن، آستانه سهمیه بنزین و شخصی‌سازی هشدارهای برنامه",
                            fontSize = 12.sp,
                            color = Color(0xFFBAE6FD),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
          }
        }

        // =====================================================================
        // 2. ACTIVE SMART NOTIFICATION ALERTS SECTION
        // =====================================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "اطلاعیه‌های هوشمند فعال خودرو",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "${vehicles.size} وسیله نقلیه",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Alert Card 1: Insurance Expiry Alert
        if (isMasterAlertsEnabled && isInsuranceAlertEnabled) {
            item {
                SmartAlertCard(
                    title = "اطلاعیه انقضای بیمه‌نامه شخص ثالث",
                    subtitle = if (activeVehicle != null) "خودرو: ${activeVehicle.title} (${activeVehicle.formattedPlate})" else "خودرو اصلی",
                    description = "اعتبار بیمه‌نامه شخص ثالث شما رو به اتمام است. جهت جلوگیری از جریمه دیرکرد، اقدام به تمدید آنلاین فرمایید.",
                    badge = "هشدار بیمه 🛡️",
                    badgeColor = Color(0xFFD97706),
                    icon = Icons.Default.Shield,
                    gradientColors = listOf(Color(0xFF451A03), Color(0xFF7C2D12)),
                    actionText = "تمدید و درخواست بیمه 🛡️",
                    onActionClick = onNavigateToInsurance
                )
            }
        }

        // Alert Card 2: Fuel Quota Alert
        if (isMasterAlertsEnabled && isFuelQuotaAlertEnabled) {
            item {
                SmartAlertCard(
                    title = "اطلاعیه آستانه سهمیه بنزین ماهانه",
                    subtitle = "سهمیه یارانه ماهانه ۱۵۰۰ تومانی",
                    description = "موجودی سهمیه کارت سوخت هوشمند شما به کمتر از $fuelQuotaPercentThreshold٪ رسید. سهمیه جدید اول ماه شارژ می‌گردد.",
                    badge = "سهمیه سوخت ⛽",
                    badgeColor = Color(0xFF0284C7),
                    icon = Icons.Default.LocalGasStation,
                    gradientColors = listOf(Color(0xFF0F172A), Color(0xFF0369A1)),
                    actionText = "مدیریت و ثبت سوخت‌گیری ⛽",
                    onActionClick = onNavigateToFuel
                )
            }
        }

        // Alert Card 3: Periodic Service & Oil Change Alert
        if (isMasterAlertsEnabled && isServiceAlertEnabled) {
            item {
                SmartAlertCard(
                    title = "اطلاعیه سرویس دوره‌ای و تعویض روغن",
                    subtitle = "سرویس موتور و فیلترها",
                    description = "بر اساس پیمایش ثبت‌شده، کمتر از $serviceAlertKmThreshold کیلومتر تا زمان تعویض روغن موتور و فیلترها باقی مانده است.",
                    badge = "یادآور فنی 🛢️",
                    badgeColor = Color(0xFF059669),
                    icon = Icons.Default.Build,
                    gradientColors = listOf(Color(0xFF064E3B), Color(0xFF047857)),
                    actionText = "ثبت کیلومتر و سرویس 🛠️",
                    onActionClick = onNavigateToFuel
                )
            }
        }

        // Alert Card 4: Technical Inspection Alert
        if (isMasterAlertsEnabled) {
            item {
                SmartAlertCard(
                    title = "اطلاعیه معاینه فنی خودرو",
                    subtitle = "گواهی سلامت فنی و آلایندگی",
                    description = "گواهی معاینه فنی خودرو معتبر است. جهت اطمینان می‌توانید تاریخ دقیق انقضا را استعلام فرمایید.",
                    badge = "معاینه فنی 🚗",
                    badgeColor = Color(0xFF7C3AED),
                    icon = Icons.Default.Verified,
                    gradientColors = listOf(Color(0xFF2E1065), Color(0xFF5B21B6)),
                    actionText = "استعلام معاینه فنی 🔍",
                    onActionClick = onNavigateToInquiry
                )
            }
        }

        // =====================================================================
        // 2.5 QUICK PERIODIC SERVICE REMINDER SETTINGS CARD (ثبت یادآور سریع سرویس)
        // =====================================================================
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(animationSpec = tween(600), initialOffsetY = { 50 })
            ) {
                QuickServiceReminderCard(
                    vehicles = vehicles,
                    activeVehicleId = selectedVehicleId,
                    onAddReminder = { vehicleId, serviceType, targetKm, targetDate ->
                        fuelViewModel.addServiceReminder(
                            vehicleId = vehicleId,
                            serviceType = serviceType,
                            targetDateMillis = System.currentTimeMillis() + (30 * 24 * 3600 * 1000L), // Fallback
                            targetOdometer = targetKm,
                            notes = "ثبت شده از داشبورد هوشمند (تاریخ: $targetDate)"
                        )
                    }
                )
            }
        }

        // =====================================================================
        // 3. NOTIFICATION CUSTOMIZATION PANEL (شخصی‌سازی اعلان‌ها)
        // =====================================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notification_customization_panel"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "شخصی‌سازی هشدارهای برنامه",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "تنظیم آستانه‌ها، زمان‌بندی و نوع اعلان‌های هوشمند",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Master Toggle Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "فعال‌سازی کلی اطلاعیه‌های هوشمند",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "نمایش تمام کارت‌های هشدار سوخت، بیمه و یادآورها در داشبورد",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isMasterAlertsEnabled,
                            onCheckedChange = { isMasterAlertsEnabled = it }
                        )
                    }

                    AnimatedVisibility(
                        visible = isMasterAlertsEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // 1. Insurance Alert Customization
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🛡️ اعلان انقضای بیمه‌نامه شخص ثالث",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Switch(
                                        checked = isInsuranceAlertEnabled,
                                        onCheckedChange = { isInsuranceAlertEnabled = it }
                                    )
                                }

                                if (isInsuranceAlertEnabled) {
                                    Text(
                                        text = "آستانه هشدار پیش از انقضا:",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(30, 15, 7, 3).forEach { days ->
                                            FilterChip(
                                                selected = insuranceAlertDaysThreshold == days,
                                                onClick = { insuranceAlertDaysThreshold = days },
                                                label = { Text("$days روز قبل", fontSize = 10.5.sp) }
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // 2. Service & Oil Alert Customization
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🛢️ اعلان سرویس دوره‌ای و تعویض روغن",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Switch(
                                        checked = isServiceAlertEnabled,
                                        onCheckedChange = { isServiceAlertEnabled = it }
                                    )
                                }

                                if (isServiceAlertEnabled) {
                                    Text(
                                        text = "آستانه هشدار کیلومتر باقی‌مانده:",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(1000, 500, 200).forEach { km ->
                                            FilterChip(
                                                selected = serviceAlertKmThreshold == km,
                                                onClick = { serviceAlertKmThreshold = km },
                                                label = { Text("$km کیلومتر قبل", fontSize = 10.5.sp) }
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // 3. Fuel Quota Alert Customization
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⛽ هشدار اتمام سهمیه بنزین ماهانه",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Switch(
                                        checked = isFuelQuotaAlertEnabled,
                                        onCheckedChange = { isFuelQuotaAlertEnabled = it }
                                    )
                                }

                                if (isFuelQuotaAlertEnabled) {
                                    Text(
                                        text = "آستانه درصد باقیمانده سهمیه:",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(30, 20, 10).forEach { pct ->
                                            FilterChip(
                                                selected = fuelQuotaPercentThreshold == pct,
                                                onClick = { fuelQuotaPercentThreshold = pct },
                                                label = { Text("کمتر از $pct٪", fontSize = 10.5.sp) }
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // 4. Sound & Vibration & Banner Toggles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔊 پخش هشدار صوتی و ویبره", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                Checkbox(
                                    checked = isSoundVibrationEnabled,
                                    onCheckedChange = { isSoundVibrationEnabled = it }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💬 اعلان‌های بنر شناور (Floating Banner)", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                Checkbox(
                                    checked = isFloatingBannerEnabled,
                                    onCheckedChange = { isFloatingBannerEnabled = it }
                                )
                            }
                        }
                    }

                    // Save Button
                    Button(
                        onClick = saveNotificationSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ذخیره تنظیمات شخصی‌سازی اعلان‌ها",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartAlertCard(
    title: String,
    subtitle: String,
    description: String,
    badge: String,
    badgeColor: Color,
    icon: ImageVector,
    gradientColors: List<Color>,
    actionText: String,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.2.dp, badgeColor.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = Color.White)
                            Text(subtitle, fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 17.sp
                )

                Button(
                    onClick = onActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                ) {
                    Text(actionText, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowBackIos, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickServiceReminderCard(
    vehicles: List<VehicleEntity>,
    activeVehicleId: Long?,
    onAddReminder: (vehicleId: Long, serviceType: String, targetKm: Int, targetDate: String) -> Unit
) {
    var selectedVehicle by remember(vehicles, activeVehicleId) {
        mutableStateOf(vehicles.firstOrNull { it.id == activeVehicleId } ?: vehicles.firstOrNull())
    }
    var serviceType by remember { mutableStateOf("") }
    var targetKm by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    var expandedVehicleMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_service_reminder_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "ثبت سریع یادآور سرویس دوره‌ای",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ثبت کیلومتر و تاریخ تعویض روغن، لنت و فیلترها",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            if (vehicles.isEmpty()) {
                Text(
                    text = "⚠️ لطفا ابتدا یک وسیله نقلیه در بخش مدیریت سوخت ثبت کنید.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                // Vehicle Selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("انتخاب وسیله نقلیه:", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedVehicleMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedVehicle?.let { "${it.title} (${it.formattedPlate})" } ?: "انتخاب خودرو",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = expandedVehicleMenu,
                            onDismissRequest = { expandedVehicleMenu = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            vehicles.forEach { v ->
                                DropdownMenuItem(
                                    text = { Text("${v.title} (${v.formattedPlate})") },
                                    onClick = {
                                        selectedVehicle = v
                                        expandedVehicleMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Service Type Input
                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("نوع سرویس (مانند: تعویض روغن موتور)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Target Mileage Input
                    OutlinedTextField(
                        value = targetKm,
                        onValueChange = { targetKm = it },
                        label = { Text("کیلومتر هدف تعویض") },
                        placeholder = { Text("مثال: ۸۵۰۰۰") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Target Date Input
                    OutlinedTextField(
                        value = targetDate,
                        onValueChange = { targetDate = it },
                        label = { Text("تاریخ هدف (شمسی)") },
                        placeholder = { Text("مثال: ۱۴۰۵/۰۹/۱۵") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Button(
                    onClick = {
                        val vId = selectedVehicle?.id
                        if (vId == null) {
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("لطفاً یک وسیله نقلیه انتخاب کنید")
                            return@Button
                        }
                        if (serviceType.isBlank()) {
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وارد کردن نوع سرویس الزامی است")
                            return@Button
                        }
                        val km = targetKm.toIntOrNull()
                        if (km == null || km <= 0) {
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("کیلومتر هدف معتبر وارد کنید")
                            return@Button
                        }
                        if (targetDate.isBlank()) {
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وارد کردن تاریخ هدف الزامی است")
                            return@Button
                        }

                        onAddReminder(vId, serviceType, km, targetDate)
                        serviceType = ""
                        targetKm = ""
                        targetDate = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ثبت و فعال‌سازی یادآور سرویس", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
            }
        }
    }
}
