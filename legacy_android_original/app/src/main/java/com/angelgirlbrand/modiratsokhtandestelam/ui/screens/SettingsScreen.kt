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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
        if (submissionMessage != null) {
            Toast.makeText(context, submissionMessage, Toast.LENGTH_LONG).show()
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

        // ================= 1. THEME & APPEARANCE CARD =================
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "شخصی‌سازی تم و ظاهر برنامه",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "انتخاب رنگ اصلی و تم شب/روز به دلخواه شما",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Sky Blue Theme Active Banner
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ColorLens,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "تم رنگی فعال: آبی آسمانی یکدست (Sky Blue)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "رابط کاربری مدرن، چشم‌نواز و متناسب با استانداردهای طراحی متریال ۳",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Dark / Light Mode Preference
                    Text(
                        text = "حالت نمایش (تاریک / روشن):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf(
                            Triple(DarkModePref.LIGHT, "روشن (لایت)", Icons.Default.LightMode),
                            Triple(DarkModePref.DARK, "تاریک (دارک)", Icons.Default.DarkMode),
                            Triple(DarkModePref.SYSTEM, "خودکار", Icons.Default.BrightnessAuto)
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
                                        Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
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

        // ================= 3. DEVELOPER SUPPORT & DONATION CARD =================
        item {
            val cardNumber = "6219861920696209"
            val cardHolder = "میلاد قنواتی - برنامه نویس برنامه"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "برنامه‌نویس: میلاد قنواتی (angelgirlbrand)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "نسخه ۱.۰ | سامانه هوشمند مدیریت خودرو",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Bank card box
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "شماره کارت جهت حمایت مالی از توسعه برنامه:",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "۶۲۱۹ - ۸۶۱۹ - ۲۰۶۹ - ۶۲۰۹",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF38BDF8),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cardHolder,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("شماره کارت", cardNumber)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "شماره کارت کپی شد: $cardNumber", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("کپی شماره کارت", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showDonateDialog = true },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ثبت و ارسال فیش حمایت", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "ارتباط مستقیم با ادمین در پیام‌رسان‌ها (@angelgirlbrand):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ble.ir/angelgirlbrand"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "شناسه بله: @angelgirlbrand", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("بله", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rubika.ir/angelgirlbrand"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "شناسه روبیکا: @angelgirlbrand", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("روبیکا", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/angelgirlbrand"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "شناسه تلگرام: @angelgirlbrand", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("تلگرام", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
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
                Toast.makeText(context, "رمز عبور با موفقیت فعال شد", Toast.LENGTH_SHORT).show()
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
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.VolunteerActivism,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حمایت مالی از برنامه‌نویس",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "بستن")
                        }
                    }

                    Text(
                        text = "مبلغ واریزی دلخواه را انتخاب یا وارد کرده و در صورت تمایل تصویر فیش را ضمیمه فرمایید:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Preset amount chips + Custom option
                item {
                    Text(
                        text = "انتخاب مبلغ حمایت:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

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
                }

                // Custom amount text field if selected
                if (selectedPresetIndex == 4) {
                    item {
                        OutlinedTextField(
                            value = customAmount,
                            onValueChange = { if (it.all { c -> c.isDigit() }) customAmount = it },
                            label = { Text("مبلغ دلخواه شما (تومان)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Attach receipt photo section
                item {
                    Text(
                        text = "تصویر یا رسید واریز (اختیاری):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

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
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("انتخاب و پیوست عکس فیش واریزی", fontSize = 12.sp)
                        }
                    }
                }

                // Payer Name & Phone
                item {
                    OutlinedTextField(
                        value = payerName,
                        onValueChange = { payerName = it },
                        label = { Text("نام پرداخت‌کننده (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("پیام یا توضیحات به برنامه‌نویس (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                // Action buttons
                item {
                    Spacer(modifier = Modifier.height(4.dp))
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("در حال ارسال به ربات...")
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ارسال فیش و ثبت حمایت مالی")
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "تعیین رمز عبور ۴ رقمی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pin = it },
                    label = { Text("رمز ۴ رقمی") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (pin.length == 4) onConfirm(pin) },
                        enabled = pin.length == 4,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("تایید رمز")
                    }
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                        Text("انصراف")
                    }
                }
            }
        }
    }
}
