package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import kotlinx.coroutines.delay

// =============================================================================
// 1. DYNAMIC STATUS WAITING TRACKER (3 CYCLING COLORS & PULSE ANIMATION)
// =============================================================================

data class StatusMessageStep(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val primaryColor: Color,
    val backgroundColor: Color,
    val borderColor: Color,
    val glowColor: Color
)

@Composable
fun DynamicStatusWaitingTracker(
    modifier: Modifier = Modifier,
    isProcessing: Boolean = true,
    latestStatusMessage: String? = null
) {
    // 3 Smooth Cycling Colors: 1. Orange/Amber (🟠) -> 2. Sky Blue (🔵) -> 3. Emerald Green (🟢)
    val statusSteps = remember {
        listOf(
            // Step 1: Warm Orange / Amber (نارنجی)
            StatusMessageStep(
                title = "در حال بررسی توسط ادمین...",
                description = "اطلاعات پلاک و درخواست شما در صف بررسی کارشناس قرار گرفت.",
                icon = Icons.Default.HourglassTop,
                primaryColor = Color(0xFFFB923C),
                backgroundColor = Color(0xFF0F172A),
                borderColor = Color(0xFFEA580C),
                glowColor = Color(0xFFF97316)
            ),
            // Step 2: Sky Blue / Ocean (آبی)
            StatusMessageStep(
                title = "درخواست در حال پردازش...",
                description = "ارتباط با سرور پیگیری و صدور رسید دیجیتال برقرار گردید.",
                icon = Icons.Default.Sync,
                primaryColor = Color(0xFF38BDF8),
                backgroundColor = Color(0xFF0F172A),
                borderColor = Color(0xFF0284C7),
                glowColor = Color(0xFF0EA5E9)
            ),
            // Step 3: Fresh Emerald Green (سبز)
            StatusMessageStep(
                title = "در حال تطبیق و صدور تاییدیه...",
                description = "کارشناس در حال تطبیق نهایی مدارک است؛ لطفاً شکیبا باشید.",
                icon = Icons.Default.CheckCircle,
                primaryColor = Color(0xFF34D399),
                backgroundColor = Color(0xFF0F172A),
                borderColor = Color(0xFF059669),
                glowColor = Color(0xFF10B981)
            )
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            while (true) {
                delay(5500) // Calm, unhurried and relaxed reading pace (انیمیشن آرام و باوقار)
                currentIndex = (currentIndex + 1) % statusSteps.size
            }
        }
    }

    val currentStep = statusSteps[currentIndex]

    // Gentle Animated Color Transitions (Smooth Morphing between Orange, Blue, Green)
    val animatedPrimaryColor by animateColorAsState(
        targetValue = currentStep.primaryColor,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "primaryColorAnim"
    )

    val animatedBgColor by animateColorAsState(
        targetValue = currentStep.backgroundColor,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "bgColorAnim"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = currentStep.borderColor,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "borderColorAnim"
    )

    val animatedGlowColor by animateColorAsState(
        targetValue = currentStep.glowColor,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "glowColorAnim"
    )

    // Pulse Animation (افکت نبضی آرام، ملایم، ریتمیک و چشم‌نواز با سرعت مناسب)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val radarRingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRingScale"
    )

    val radarRingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.60f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRingAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dynamic_status_waiting_tracker"),
        shape = RoundedCornerShape(18.dp),
        color = animatedBgColor,
        border = BorderStroke(1.4.dp, animatedBorderColor.copy(alpha = 0.75f)),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Live Pulsing Indicator with Radar Wave Ring & Changing Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pulsing Icon with Radar Ring Effect
                    Box(
                        modifier = Modifier.size(38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer Radar Pulse Ring
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(animatedGlowColor.copy(alpha = radarRingAlpha))
                        )

                        // Inner Pulsing Glow Circle
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(animatedPrimaryColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = currentStep.icon,
                                contentDescription = null,
                                tint = animatedPrimaryColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Column {
                        AnimatedContent(
                            targetState = currentStep.title,
                            transitionSpec = {
                                (fadeIn(tween(380)) + slideInVertically { it / 2 })
                                    .togetherWith(fadeOut(tween(260)) + slideOutVertically { -it / 2 })
                            },
                            label = "titleAnimation"
                        ) { titleText ->
                            Text(
                                text = titleText,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = animatedPrimaryColor
                            )
                        }
                    }
                }

                // 3 Color Step Dots with Pulsing Live Dot
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    statusSteps.forEachIndexed { index, step ->
                        val isCurrent = index == currentIndex
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 10.dp else 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) animatedPrimaryColor.copy(alpha = pulseAlpha)
                                    else step.primaryColor.copy(alpha = 0.35f)
                                )
                        )
                    }
                }
            }

            // Animated Description Text
            AnimatedContent(
                targetState = currentStep.description,
                transitionSpec = {
                    fadeIn(tween(420)).togetherWith(fadeOut(tween(220)))
                },
                label = "descAnimation"
            ) { descText ->
                Text(
                    text = descText,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.5.sp
                )
            }

            // Gradient Shimmer Progress Indicator Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(animatedBorderColor.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    animatedPrimaryColor.copy(alpha = 0.6f),
                                    animatedGlowColor,
                                    animatedPrimaryColor
                                )
                            )
                        )
                )
            }

            // Live Bale Bot Command Updates Badge
            if (!latestStatusMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = BorderStroke(0.8.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "تغییر زنده با تایید کارشناس و مدیر: $latestStatusMessage",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 2. CHIC USER REQUEST FORM DIALOG (MODERN MATERIAL 3 DESIGN)
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChicUserRequestFormDialog(
    vehicles: List<VehicleEntity>,
    initialServiceType: String = "استعلام و تسویه رسمی خلافی خودرو و موتورسیکلت",
    isSubmitting: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (
        serviceType: String,
        vehicleTitle: String,
        plateNumber: String,
        fullName: String,
        phoneNumber: String,
        nationalCode: String,
        additionalNotes: String
    ) -> Unit
) {
    val context = LocalContext.current

    val serviceCategories = listOf(
        "استعلام و تسویه رسمی خلافی خودرو و موتورسیکلت",
        "استعلام نمره منفی گواهی‌نامه و سوابق",
        "استعلام وضع گواهی‌نامه و کارت رانندگی",
        "استعلام معاینه فنی خودرو و گواهی سلامت",
        "استعلام اسناد خودرو و وضعیت پلاک فعال",
        "استعلام و پرداخت عوارض آزادراهی",
        "استعلام عوارض سالیانه شهرداری",
        "استعلام مالیات نقل و انتقال خودرو",
        "ثبت درخواست خرید و تمدید انواع بیمه‌نامه",
        "درخواست صدور کارت هوشمند سوخت"
    )

    var selectedType by remember { mutableStateOf(initialServiceType) }
    var selectedVehicle by remember { mutableStateOf(vehicles.firstOrNull()) }
    var vehiclePlate by remember { mutableStateOf(vehicles.firstOrNull()?.formattedPlate ?: "") }
    var vehicleTitle by remember { mutableStateOf(vehicles.firstOrNull()?.title ?: "خودرو سواری") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var nationalCode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFF0F172A),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .testTag("chic_user_request_form_dialog")
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header: Gradient Card with Rich Styling
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
                        .padding(horizontal = 20.dp, vertical = 18.dp)
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
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "فرم ثبت و ارسال درخواست",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.5.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ارسال مستقیم برای ادمین و صدور کد رهگیری",
                                    fontSize = 11.sp,
                                    color = Color(0xFFBAE6FD)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Main Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Dynamic Status Tracker if Submitting / Processing
                    if (isSubmitting) {
                        DynamicStatusWaitingTracker(
                            isProcessing = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 2. Service Category Picker
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "نوع خدمات یا استعلام مورد نظر:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { expandedDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = selectedType,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                serviceCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = cat,
                                                fontSize = 12.sp,
                                                fontWeight = if (cat == selectedType) FontWeight.Bold else FontWeight.Normal,
                                                color = if (cat == selectedType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Assignment,
                                                contentDescription = null,
                                                tint = if (cat == selectedType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            selectedType = cat
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 3. Select From User's Saved Vehicles
                    if (vehicles.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "انتخاب از وسایل نقلیه من:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(vehicles) { v ->
                                    FilterChip(
                                        selected = selectedVehicle?.id == v.id,
                                        onClick = {
                                            selectedVehicle = v
                                            vehiclePlate = v.formattedPlate
                                            vehicleTitle = v.title
                                        },
                                        label = { Text("${when { v.isMotorcycle -> "🏍️"; v.isArvand -> "🌴"; else -> "🚗" }} ${v.title} (${v.formattedPlate})", fontSize = 11.sp) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Live Authentic Plate Preview (if vehicle selected)
                    val currentSelVeh = selectedVehicle
                    if (currentSelVeh != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            VehiclePlateView(vehicle = currentSelVeh)
                        }
                    }

                    // 4. Vehicle Plate Input
                    OutlinedTextField(
                        value = vehiclePlate,
                        onValueChange = { vehiclePlate = it },
                        label = { Text("شماره پلاک وسیله نقلیه (ملی، اروندی، موتور)") },
                        placeholder = { Text("مثال: ۱۲ ب ۳۴۵ ایران ۱۱ یا اروند ۱۲۳۶۵") },
                        leadingIcon = {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 5. Applicant Personal Information
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("نام و نام خانوادگی") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("شماره همراه") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = nationalCode,
                        onValueChange = { if (it.length <= 10) nationalCode = it },
                        label = { Text("کد ملی متقاضی (اجباری - ۱۰ رقم) *") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 6. Additional Notes (Mandatory)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("توضیحات تکمیلی یا پیام برای ادمین (اجباری) *") },
                        placeholder = { Text("توضیحات مورد نیاز مانند مدل، سال ساخت، نوع بیمه‌نامه یا شناسه پرداخت...") },
                        leadingIcon = {
                            Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 7. Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (vehiclePlate.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("لطفاً شماره پلاک را وارد نمایید")
                                    return@Button
                                }
                                if (nationalCode.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وارد کردن کد ملی متقاضی الزامی است")
                                    return@Button
                                }
                                if (notes.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وارد کردن توضیحات تکمیلی الزامی است")
                                    return@Button
                                }
                                onSubmit(
                                    selectedType,
                                    vehicleTitle,
                                    vehiclePlate.trim(),
                                    fullName.trim(),
                                    phoneNumber.trim(),
                                    nationalCode.trim(),
                                    notes.trim()
                                )
                                // Explicit user-facing confirmation
                                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("درخواست برای ادمین ارسال شد")
                            },
                            enabled = !isSubmitting && vehiclePlate.isNotBlank() && nationalCode.isNotBlank() && notes.isNotBlank(),
                            modifier = Modifier
                                .weight(1.35f)
                                .height(48.dp)
                                .testTag("submit_request_for_admin_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("در حال پردازش...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ارسال برای ادمین 🚀", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(0.85f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("انصراف", fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }
    }
}
}
