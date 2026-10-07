package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity

@Composable
fun VehicleFormDialog(
    vehicleToEdit: VehicleEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        plateF2: String,
        plateLetter: String,
        plateL3: String,
        plateCity: String,
        fuelType: String,
        tankCapacity: Double,
        odometer: Int,
        vehicleType: String
    ) -> Unit
) {
    val isEditing = vehicleToEdit != null
    var vehicleType by remember { mutableStateOf(vehicleToEdit?.vehicleType ?: VehicleEntity.TYPE_CAR) }
    var title by remember { mutableStateOf(vehicleToEdit?.title.orEmpty()) }
    var plateF2 by remember { mutableStateOf(vehicleToEdit?.plateFirst2.orEmpty()) }
    var plateLetter by remember { mutableStateOf(vehicleToEdit?.plateLetter?.ifEmpty { "ب" } ?: "ب") }
    var plateL3 by remember { mutableStateOf(vehicleToEdit?.plateLast3.orEmpty()) }
    var plateCity by remember { mutableStateOf(vehicleToEdit?.plateCityCode?.ifEmpty { "11" } ?: "11") }
    var arvandDigits by remember {
        mutableStateOf(
            if (vehicleToEdit?.isArvand == true) vehicleToEdit.plateLast3.ifEmpty { vehicleToEdit.plateFirst2 } else "12365"
        )
    }
    var fuelType by remember { mutableStateOf(vehicleToEdit?.fuelType?.ifEmpty { "بنزین معمولی" } ?: "بنزین معمولی") }
    var tankCapacity by remember {
        mutableStateOf(
            if (vehicleToEdit != null) vehicleToEdit.tankCapacity.toInt().toString()
            else if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) "10" else "50"
        )
    }
    var currentOdometer by remember { mutableStateOf((vehicleToEdit?.currentOdometer ?: 0).toString()) }

    val fuelTypes = if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) {
        listOf("بنزین معمولی", "بنزین سوپر")
    } else {
        listOf("بنزین معمولی", "بنزین سوپر", "دوگانه‌سوز CNG", "گازوئیل")
    }

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
                    .padding(vertical = 12.dp)
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
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
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
                                        if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isEditing) "ویرایش اطلاعات وسیله نقلیه" else "ثبت وسیله نقلیه جدید",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "مدیریت سوخت، پلاک و استعلام‌های مرتبط",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Vehicle Type Selector (خودرو، پلاک اروندی، موتورسیکلت)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "نوع وسیله نقلیه و فرمت پلاک:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = vehicleType == VehicleEntity.TYPE_CAR,
                                onClick = {
                                    vehicleType = VehicleEntity.TYPE_CAR
                                    if (tankCapacity == "10") tankCapacity = "50"
                                },
                                label = { Text("🚗 خودرو ملی", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            FilterChip(
                                selected = vehicleType == VehicleEntity.TYPE_ARVAND,
                                onClick = {
                                    vehicleType = VehicleEntity.TYPE_ARVAND
                                    if (tankCapacity == "10") tankCapacity = "50"
                                },
                                label = { Text("🌴 اروندی", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            FilterChip(
                                selected = vehicleType == VehicleEntity.TYPE_MOTORCYCLE,
                                onClick = {
                                    vehicleType = VehicleEntity.TYPE_MOTORCYCLE
                                    if (tankCapacity == "50") tankCapacity = "10"
                                },
                                label = { Text("🏍️ موتورسیکلت", fontSize = 11.sp) },
                                modifier = Modifier.weight(1.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // 2. Title / Model
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = {
                            Text(
                                when (vehicleType) {
                                    VehicleEntity.TYPE_ARVAND -> "مدل خودرو اروندی (مثلاً: اسپورتیج، اپتیما، کامارو)"
                                    VehicleEntity.TYPE_MOTORCYCLE -> "مدل موتورسیکلت (مثلاً: هوندا ۱۲۵، کلیک)"
                                    else -> "مدل خودرو (مثلاً: پژو ۲۰۶، دنا پلاس، سمند)"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 3. Live Plate Preview (تغییر خودکار رنگ متناسب با پلاک انتخابی)
                    Text(
                        text = when (vehicleType) {
                            VehicleEntity.TYPE_ARVAND -> "پیش‌نمایش پلاک منطقه آزاد اروند (فارسی و انگلیسی):"
                            VehicleEntity.TYPE_MOTORCYCLE -> "پیش‌نمایش پلاک ملی موتورسیکلت:"
                            else -> "پیش‌نمایش پلاک خودرو (تغییر خودکار رنگ):"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SmartAdaptivePlateView(
                            first2 = plateF2,
                            letter = if (vehicleType == VehicleEntity.TYPE_ARVAND) "اروند" else if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) "موتور" else plateLetter,
                            last3 = plateL3,
                            cityCode = plateCity,
                            isArvand = vehicleType == VehicleEntity.TYPE_ARVAND,
                            arvandNumber = arvandDigits,
                            isMotorcycle = vehicleType == VehicleEntity.TYPE_MOTORCYCLE,
                            motoTop3 = plateF2,
                            motoBottom5 = plateL3
                        )
                    }

                    when (vehicleType) {
                        VehicleEntity.TYPE_ARVAND -> {
                            OutlinedTextField(
                                value = arvandDigits,
                                onValueChange = { if (it.length <= 5) arvandDigits = it },
                                label = { Text("شماره ۵ رقمی پلاک اروند (مثلاً ۱۲۳۶۵)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                supportingText = {
                                    Text("ارقام در ردیف بالا به فارسی و در ردیف پایین به انگلیسی درج می‌شوند")
                                }
                            )
                        }
                        VehicleEntity.TYPE_MOTORCYCLE -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = plateF2,
                                    onValueChange = { if (it.length <= 3) plateF2 = it },
                                    label = { Text("۳ رقم بالا (مثلاً ۱۲۳)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = plateL3,
                                    onValueChange = { if (it.length <= 5) plateL3 = it },
                                    label = { Text("۵ رقم پایین (مثلاً ۴۵۶۷۸)") },
                                    modifier = Modifier.weight(1.3f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = plateCity,
                                    onValueChange = { if (it.length <= 2) plateCity = it },
                                    label = { Text("ایران") },
                                    modifier = Modifier.weight(0.9f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = plateL3,
                                    onValueChange = { if (it.length <= 3) plateL3 = it },
                                    label = { Text("۳ رقم") },
                                    modifier = Modifier.weight(1.1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                // Letter picker with live colors
                                Box(modifier = Modifier.weight(1.1f)) {
                                    IranianPlateLetterPicker(
                                        selectedLetter = plateLetter,
                                        onLetterSelected = { plateLetter = it }
                                    )
                                }
                                OutlinedTextField(
                                    value = plateF2,
                                    onValueChange = { if (it.length <= 2) plateF2 = it },
                                    label = { Text("۲ رقم") },
                                    modifier = Modifier.weight(0.9f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    // 4. Fuel type & Tank Capacity & Odometer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tankCapacity,
                            onValueChange = { tankCapacity = it },
                            label = { Text("ظرفیت باک (لیتر)") },
                            leadingIcon = {
                                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = currentOdometer,
                            onValueChange = { currentOdometer = it },
                            label = { Text("کارکرد (کیلومتر)") },
                            leadingIcon = {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Fuel type chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("نوع سوخت:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            fuelTypes.forEach { ft ->
                                FilterChip(
                                    selected = fuelType == ft,
                                    onClick = { fuelType = ft },
                                    label = { Text(ft, fontSize = 10.5.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    // Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onConfirm(
                                        title.trim(),
                                        if (vehicleType == VehicleEntity.TYPE_ARVAND) arvandDigits.trim() else plateF2.trim(),
                                        if (vehicleType == VehicleEntity.TYPE_ARVAND) "اروند" else if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) "موتور" else plateLetter.trim(),
                                        if (vehicleType == VehicleEntity.TYPE_ARVAND) arvandDigits.trim() else plateL3.trim(),
                                        if (vehicleType == VehicleEntity.TYPE_ARVAND) "اروند" else plateCity.trim(),
                                        fuelType,
                                        tankCapacity.toDoubleOrNull() ?: (if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) 10.0 else 50.0),
                                        currentOdometer.toIntOrNull() ?: 0,
                                        vehicleType
                                    )
                                }
                            },
                            enabled = title.isNotBlank(),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEditing) "ذخیره تغییرات" else "ثبت اطلاعات", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(0.85f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp)
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
