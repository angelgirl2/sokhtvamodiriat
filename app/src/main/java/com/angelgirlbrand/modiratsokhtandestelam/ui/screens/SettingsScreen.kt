package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.angelgirlbrand.modiratsokhtandestelam.security.AppThemeColor
import com.angelgirlbrand.modiratsokhtandestelam.security.DarkModePref
import com.angelgirlbrand.modiratsokhtandestelam.security.SecurityManager
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.M3ThemedDialogContainer
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel

@Composable
fun SettingsScreen(
    securityManager: SecurityManager,
    baleViewModel: BaleServiceViewModel,
    fuelViewModel: FuelViewModel,
    currentThemeColor: AppThemeColor = AppThemeColor.SKY_BLUE,
    currentDarkModePref: DarkModePref = DarkModePref.SYSTEM,
    onThemeColorChanged: (AppThemeColor) -> Unit = {},
    onDarkModePrefChanged: (DarkModePref) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSubmitting by baleViewModel.isSubmitting.collectAsState()
    val submissionMessage by baleViewModel.submissionMessage.collectAsState()

    var isPinEnabled by remember { mutableStateOf(securityManager.isPinEnabled()) }
    var isBiometricEnabled by remember { mutableStateOf(securityManager.isBiometricEnabled()) }

    var showPinDialog by remember { mutableStateOf(false) }
    var showDonateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(submissionMessage) {
        submissionMessage?.let { msg ->
            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show(msg)
            baleViewModel.clearMessage()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "تنظیمات و شخصی‌سازی سامانه",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "تغییر تم رنگی، حالت تاریک/روشن، امنیت و حمایت مالی",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ================= 1. THEME & APPEARANCE CARD (DARK / LIGHT / SYSTEM ONLY) =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_appearance_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.BrightnessMedium,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "حالت نمایش و تم برنامه",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تنظیم حالت شب و روز به دلخواه شما",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Dark / Light / Auto Mode Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf(
                            Triple(DarkModePref.LIGHT, "روشن (لایت)", Icons.Default.LightMode),
                            Triple(DarkModePref.DARK, "تاریک (دارک)", Icons.Default.DarkMode),
                            Triple(DarkModePref.SYSTEM, "خودکار سیستم", Icons.Default.BrightnessAuto)
                        )

                        modes.forEach { (mode, label, icon) ->
                            val isSelected = currentDarkModePref == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { onDarkModePrefChanged(mode) },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // ================= 1.1 ONBOARDING & INTERACTIVE FEATURE GUIDES RESET CARD =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_guides_reset_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "راهنما و آموزش برنامه (تور معرفی)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "مدیریت راهنماهای گام‌به‌گام درون‌برنامه‌ای",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "راهنماهای بالای هر صفحه و اسلایدهای معرفی برنامه فقط یک‌بار برای کاربران جدید نمایش داده می‌شوند. در صورت نیاز به مرور مجدد، می‌توانید آن‌ها را بازنشانی نمایید.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = {
                            securityManager.resetAllGuidesAndOnboarding()
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("✅ راهنماهای برنامه بازنشانی شدند. در ورود مجدد به هر بخش نمایش داده خواهند شد.")
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بازنشانی و مشاهده مجدد راهنماها",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }

        // ================= 1.1 NOTIFICATIONS & REMINDERS CUSTOM SETTINGS CARD =================
        item {
            val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
            var isServiceReminderEnabled by remember { mutableStateOf(prefs.getBoolean("service_reminders_enabled", true)) }
            var isInsuranceReminderEnabled by remember { mutableStateOf(prefs.getBoolean("insurance_reminders_enabled", true)) }
            var isSystemNotifEnabled by remember { mutableStateOf(prefs.getBoolean("system_notif_enabled", true)) }
            var isInAppBannerEnabled by remember { mutableStateOf(prefs.getBoolean("in_app_banner_enabled", true)) }
            var isVibrateEnabled by remember { mutableStateOf(prefs.getBoolean("vibrate_enabled", false)) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notifications_settings_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تنظیمات اعلان‌ها و یادآورها",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "شخصی‌سازی هشدار سرویس و انقضای بیمه‌نامه",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    // 1. Service Reminders Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("یادآورهای سرویس دوره‌ای", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                            Text("دریافت اعلان برای تعویض روغن، فیلتر و آلارم های صوتی", fontSize = 10.5.sp, color = Color(0xFFCBD5E1))
                        }
                        Switch(
                            checked = isServiceReminderEnabled,
                            onCheckedChange = { 
                                isServiceReminderEnabled = it
                                prefs.edit().putBoolean("service_reminders_enabled", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8))
                        )
                    }

                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f))

                    // 2. Insurance Reminders Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("هشدار انقضای بیمه‌نامه", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                            Text("دریافت آلارم صوتی و تصویری قبل از سررسید انقضا", fontSize = 10.5.sp, color = Color(0xFFCBD5E1))
                        }
                        Switch(
                            checked = isInsuranceReminderEnabled,
                            onCheckedChange = { 
                                isInsuranceReminderEnabled = it
                                prefs.edit().putBoolean("insurance_reminders_enabled", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8))
                        )
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Text("نوع اعلان‌ها و فیدبک:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF38BDF8))

                    // A. System Notification
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                            Text("اعلان سیستمی اندروید (Push)", fontSize = 11.5.sp, color = Color.White)
                        }
                        Checkbox(
                            checked = isSystemNotifEnabled,
                            onCheckedChange = { 
                                isSystemNotifEnabled = it
                                prefs.edit().putBoolean("system_notif_enabled", it).apply()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0284C7))
                        )
                    }

                    // B. In-App Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.FeaturedVideo, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                            Text("بنر درون‌برنامه‌ای (In-App Popup)", fontSize = 11.5.sp, color = Color.White)
                        }
                        Checkbox(
                            checked = isInAppBannerEnabled,
                            onCheckedChange = { 
                                isInAppBannerEnabled = it
                                prefs.edit().putBoolean("in_app_banner_enabled", it).apply()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0284C7))
                        )
                    }

                    // C. Vibration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                            Text("هشدار لرزشی ممتد دستگاه", fontSize = 11.5.sp, color = Color.White)
                        }
                        Checkbox(
                            checked = isVibrateEnabled,
                            onCheckedChange = { 
                                isVibrateEnabled = it
                                prefs.edit().putBoolean("vibrate_enabled", it).apply()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0284C7))
                        )
                    }
                }
            }
        }

        // ================= 2. SECURITY & BIOMETRICS CARD =================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "امنیت و قفل برنامه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = "محافظت از اطلاعات خودرو و گزارش‌ها با رمز و بیومتریک", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("قفل با رمز عبور ۴ رقمی", fontWeight = FontWeight.Medium)
                            Text("هنگام ورود به برنامه رمز درخواست می‌شود", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showPinDialog = true
                                } else {
                                    securityManager.removePin()
                                    isPinEnabled = false
                                    isBiometricEnabled = false
                                }
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ورود با اثر انگشت (بیومتریک)", fontWeight = FontWeight.Medium)
                            Text("احراز هویت سریع با سنسور دستگاه", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            enabled = isPinEnabled,
                            onCheckedChange = {
                                securityManager.setBiometricEnabled(it)
                                isBiometricEnabled = it
                            }
                        )
                    }
                }
            }
        }

        // ================= 3. DEVELOPER BANK CARD & ADMIN CONTACTS CARD =================
        item {
            val cardNumberRaw = "6219861920696209"
            val cardNumberFormatted = "6219-8619-2069-6209"
            val cardHolder = "مدیریت سامانه (angelgirlbrand)"
            val adminHandle = "@angelgirlbrand"

            val copyToClipboard: (String, String) -> Unit = { label, text ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(label, text)
                clipboard.setPrimaryClip(clip)
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("📋 $label کپی شد: $text")
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // A) REALISTIC LUXURY BANK CARD (NO SHEBA / NO IBAN)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("luxury_bank_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                ) {
                    Column {
                        // Card Visual Body (Navy Slate Gradient with Metallic Chip)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0F172A),
                                            Color(0xFF1E293B),
                                            Color(0xFF0369A1)
                                        )
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                // Top Row: Bank Name & Contactless Symbol
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
                                            color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.CreditCard,
                                                    contentDescription = null,
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "بانک سامان",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.5.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "عضو شبکه شتاب 💳",
                                                fontSize = 9.5.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }

                                    // Metallic Chip & Contactless Symbol
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(5.dp),
                                            color = Color(0xFFEAB308),
                                            modifier = Modifier.size(width = 30.dp, height = 22.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .border(0.8.dp, Color(0xFF854D0E), RoundedCornerShape(5.dp))
                                            )
                                        }
                                        Icon(
                                            Icons.Default.Nfc,
                                            contentDescription = "Contactless",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // Formatted Card Number
                                Text(
                                    text = cardNumberFormatted,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF38BDF8),
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )

                                // Cardholder Name
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "نام و نام خانوادگی صاحب حساب:",
                                            fontSize = 9.5.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = cardHolder,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "کارت فعال ⚡",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }
                            }
                        }

                        // Smart Copy & Donation Action Buttons
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { copyToClipboard("شماره کارت بانک سامان", cardNumberRaw) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("کپی هوشمند شماره کارت بانکی", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showDonateDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ثبت فیش حمایت مالی و انتقال 💳", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // B) DEDICATED ADMIN CONTACT INFORMATION CARD (DARK THEME - BALE, RUBIKA, TELEGRAM ONLY)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_contacts_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header Title (Dark Theme)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.SupportAgent,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "ارتباط مستقیم و پشتیبانی ادمین",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "پاسخگویی سریع ۲۴/۷ در پیام‌رسان‌های بله، روبیکا و تلگرام",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFF334155))

                        // 1. BALE MESSENGER ITEM (بله) - Dark Theme
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF064E3B).copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Chat,
                                                contentDescription = "Bale",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text("پشتیبانی بله (Bale)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color(0xFF6EE7B7))
                                        Text("آیدی بله: $adminHandle", fontSize = 11.sp, color = Color(0xFFA7F3D0))
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { copyToClipboard("آیدی بله", adminHandle) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981).copy(alpha = 0.25f))
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "کپی آیدی بله", tint = Color(0xFF6EE7B7), modifier = Modifier.size(16.dp))
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ble.ir/angelgirlbrand"))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("شناسه بله: $adminHandle")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("گفتگو", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        // 2. RUBIKA MESSENGER ITEM (روبیکا) - Dark Theme
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF581C87).copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFA855F7),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Forum,
                                                contentDescription = "Rubika",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text("پشتیبانی روبیکا (Rubika)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color(0xFFE9D5FF))
                                        Text("آیدی روبیکا: $adminHandle", fontSize = 11.sp, color = Color(0xFFF3E8FF))
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { copyToClipboard("آیدی روبیکا", adminHandle) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFA855F7).copy(alpha = 0.25f))
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "کپی آیدی روبیکا", tint = Color(0xFFE9D5FF), modifier = Modifier.size(16.dp))
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rubika.ir/angelgirlbrand"))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("شناسه روبیکا: $adminHandle")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("گفتگو", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        // 3. TELEGRAM MESSENGER ITEM (تلگرام) - Dark Theme
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF075985).copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Send,
                                                contentDescription = "Telegram",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text("پشتیبانی تلگرام (Telegram)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color(0xFFBAE6FD))
                                        Text("آیدی تلگرام: $adminHandle", fontSize = 11.sp, color = Color(0xFFE0F2FE))
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { copyToClipboard("آیدی تلگرام", adminHandle) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0284C7).copy(alpha = 0.25f))
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "کپی آیدی تلگرام", tint = Color(0xFFBAE6FD), modifier = Modifier.size(16.dp))
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/angelgirlbrand"))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("شناسه تلگرام: $adminHandle")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("گفتگو", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= 4. APPLICATION VERSION, CACHE MANAGEMENT & UNINSTALL DATA PRIVACY =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_version_cache_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CleaningServices,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "مدیریت کش و نسخه برنامه",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "حذف کش نسخه قبلی و پاکسازی کامل داده‌ها در حذف نصب",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "نسخه ۱.۰",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Feature 1: Automatic Cache Clearing on Update
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "پاکسازی خودکار کش در زمان آپدیت",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                                Text(
                                    text = "در زمان ارتقای نسخه، کش نسخه قبلی به صورت کاملاً خودکار حذف و نسخه جدید بدون نیاز به دخالت دستی جایگزین می‌گردد.",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Feature 2: Full Data Removal Guarantee on Uninstall
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "تضمین حذف کامل داده‌ها هنگام لغو نصب (Uninstall)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF047857)
                                )
                                Text(
                                    text = "تمامی اطلاعات محلی، سوابق و تنظیمات در زمان حذف نصب برنامه به صورت ۱۰۰٪ و بدون امکان بازیابی از دستگاه حذف می‌گردند (بک‌آپ ابری غیرفعال است).",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- PIN Setup Dialog ---
    if (showPinDialog) {
        SetPinDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                securityManager.setPin(pin)
                isPinEnabled = true
                showPinDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("رمز عبور با موفقیت فعال شد")
            }
        )
    }

    // --- Financial Support / Custom Amount & Receipt Photo Dialog ---
    if (showDonateDialog) {
        SupportAndDonationDialog(
            isSubmitting = isSubmitting,
            onDismiss = { showDonateDialog = false },
            onSubmit = { amount, customText, name, phone, note, photoBytes ->
                baleViewModel.sendDonationSupport(
                    amount = amount,
                    customAmountText = customText,
                    payerName = name,
                    payerPhone = phone,
                    note = note,
                    photoBytes = photoBytes,
                    onSuccess = {
                        showDonateDialog = false
                    }
                )
            }
        )
    }
}

// =======================================================================================
// FINANCIAL SUPPORT & DONATION DIALOG (PRESET & CUSTOM AMOUNT + RECEIPT PHOTO)
// =======================================================================================
@Composable
private fun SupportAndDonationDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Long, String, String, String, String, ByteArray?) -> Unit
) {
    val context = LocalContext.current
    var selectedPresetIndex by remember { mutableStateOf(1) } // 0: 50k, 1: 100k, 2: 200k, 3: 500k, 4: Custom
    var customAmount by remember { mutableStateOf("") }
    var payerName by remember { mutableStateOf("") }
    var payerPhone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                selectedImageBytes = inputStream?.readBytes()
                inputStream?.close()
            } catch (_: Exception) {}
        }
    }

    val presetAmounts = listOf(
        50_000L to "۵۰,۰۰۰ تومان",
        100_000L to "۱۰۰,۰۰۰ تومان",
        200_000L to "۲۰۰,۰۰۰ تومان",
        500_000L to "۵۰۰,۰۰۰ تومان"
    )

    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Theme Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF991B1B),
                                    Color(0xFFDC2626)
                                )
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 16.dp)
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
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "حمایت مالی از برنامه‌نویس",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "مدیریت سامانه (angelgirlbrand)",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFECACA)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "انتخاب مبلغ حمایت مالی:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presetAmounts.take(2).forEachIndexed { idx, (amt, label) ->
                                FilterChip(
                                    selected = selectedPresetIndex == idx,
                                    onClick = { selectedPresetIndex = idx },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (selectedPresetIndex == idx) FontWeight.Bold else FontWeight.Normal) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presetAmounts.drop(2).forEachIndexed { idx, (amt, label) ->
                                val realIdx = idx + 2
                                FilterChip(
                                    selected = selectedPresetIndex == realIdx,
                                    onClick = { selectedPresetIndex = realIdx },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (selectedPresetIndex == realIdx) FontWeight.Bold else FontWeight.Normal) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        // Custom Amount option chip
                        FilterChip(
                            selected = selectedPresetIndex == 4,
                            onClick = { selectedPresetIndex = 4 },
                            label = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("مبلغ دلخواه (وارد کردن عدد)", fontSize = 11.sp, fontWeight = if (selectedPresetIndex == 4) FontWeight.Bold else FontWeight.Normal)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Custom amount text field if selected
                    if (selectedPresetIndex == 4) {
                        OutlinedTextField(
                            value = customAmount,
                            onValueChange = { if (it.all { c -> c.isDigit() }) customAmount = it },
                            label = { Text("مبلغ دلخواه شما (تومان)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Attach receipt photo section
                    Text(
                        text = "تصویر یا رسید واریز (اختیاری):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (selectedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "فیش واریزی",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            IconButton(
                                onClick = {
                                    selectedImageUri = null
                                    selectedImageBytes = null
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "حذف عکس", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("انتخاب و پیوست عکس فیش واریزی", fontSize = 11.5.sp)
                        }
                    }

                    // Payer Name & Phone
                    OutlinedTextField(
                        value = payerName,
                        onValueChange = { payerName = it },
                        label = { Text("نام پرداخت‌کننده (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("پیام یا توضیحات به برنامه‌نویس (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val finalAmt = if (selectedPresetIndex in 0..3) {
                                    presetAmounts[selectedPresetIndex].first
                                } else {
                                    customAmount.toLongOrNull() ?: 0L
                                }
                                val customStr = if (selectedPresetIndex == 4) "$customAmount تومان" else presetAmounts[selectedPresetIndex].second
                                onSubmit(finalAmt, customStr, payerName, payerPhone, note, selectedImageBytes)
                            },
                            enabled = !isSubmitting && (selectedPresetIndex in 0..3 || customAmount.isNotBlank()),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("در حال ارسال...")
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ارسال فیش و حمایت", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(0.85f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("انصراف")
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun SetPinDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "تعیین رمز عبور ۴ رقمی",
        subtitle = "جهت محافظت از اطلاعات حساس برنامه",
        icon = Icons.Default.Lock,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pin = it },
            label = { Text("رمز ۴ رقمی") },
            leadingIcon = {
                Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { if (pin.length == 4) onConfirm(pin) },
                enabled = pin.length == 4,
                modifier = Modifier
                    .weight(1.2f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("تایید و ذخیره رمز", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(0.8f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("انصراف")
            }
        }
    }
}
