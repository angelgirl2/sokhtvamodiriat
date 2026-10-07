package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceRequestEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.CustomThemedConfirmDialog
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.DynamicStatusWaitingTracker
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.InsuranceAgencyPickerField
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.IranianPlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.SmartAdaptivePlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel
import com.angelgirlbrand.modiratsokhtandestelam.util.InsuranceAgencies
import com.angelgirlbrand.modiratsokhtandestelam.util.PersianDateHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BaleBotRequestScreen(
    baleViewModel: BaleServiceViewModel,
    fuelViewModel: FuelViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val requests by baleViewModel.serviceRequests.collectAsState()
    val isSubmitting by baleViewModel.isSubmitting.collectAsState()
    val submissionMessage by baleViewModel.submissionMessage.collectAsState()
    val vehicles by fuelViewModel.vehicles.collectAsState()

    var showInsuranceDialog by remember { mutableStateOf(false) }
    var showInquiryDialog by remember { mutableStateOf(false) }
    var selectedInquiryType by remember { mutableStateOf("استعلام خلافی خودرو") }
    var showFuelCardDialog by remember { mutableStateOf(false) }
    var selectedFilterCategory by remember { mutableStateOf("همه") }
    var selectedVehicleId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(submissionMessage) {
        if (submissionMessage != null) {
            // Standard system Toast is removed completely.
            // CustomAppNotificationBanner handles showing it beautifully at the top of MainActivity.
        }
    }

    val defaultVehicle = vehicles.firstOrNull { it.id == selectedVehicleId } ?: vehicles.firstOrNull()

    val filteredRequests = remember(requests, selectedFilterCategory) {
        when (selectedFilterCategory) {
            "بیمه" -> requests.filter { it.requestType.contains("بیمه") }
            "استعلام و عوارض" -> requests.filter {
                it.requestType.contains("خلافی") ||
                it.requestType.contains("عوارض") ||
                it.requestType.contains("مالیات")
            }
            "کارت سوخت" -> requests.filter { it.requestType.contains("کارت سوخت") }
            "در انتظار تایید" -> requests.filter { it.status == ServiceRequestEntity.STATUS_PENDING }
            "تایید شده" -> requests.filter { it.status == ServiceRequestEntity.STATUS_APPROVED }
            else -> requests
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header Banner ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سامانه درخواست خدمات و پیگیری",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "صدور و تمدید انواع بیمه‌نامه، استعلام جامع خلافی، عوارض و کارت سوخت با ثبت آنی و پیگیری مستقیم وضعیت",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 1.1 Personalized Vehicle Selector & Auto-Fill Hub ---
        if (vehicles.isNotEmpty() && defaultVehicle != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.35f))
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Text("شخصی‌سازی درخواست‌ها برای:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                            Text(defaultVehicle.title, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        // Vehicle Switcher Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(vehicles) { v ->
                                val isSelected = v.id == defaultVehicle.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedVehicleId = v.id },
                                    label = { Text(v.title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    leadingIcon = {
                                        Icon(
                                            if (v.isMotorcycle) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        // Vehicle Plate Preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("پلاک ثبتی پیش‌فرض:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            SmartAdaptivePlateView(vehicle = defaultVehicle)
                        }
                    }
                }
            }
        }

        // --- 1.2 The 3-Cycling Colors Request Status Tracker ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.2.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Timeline, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Text("گردش کار سه‌رنگ درخواست‌های شما:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                        Text("پیگیری خودکار", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                    }

                    // The 3 Cycling Colors - Dark Mode Refined (پس‌زمینه تیره و حاشیه رنگی متمایز)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // 1. Orange
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFFFB923C).copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFB923C)))
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("۱. ثبت در صف", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDBA74))
                                Text("بررسی کارشناس", fontSize = 8.sp, color = Color(0xFFFED7AA))
                            }
                        }
                        // 2. Blue
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF38BDF8)))
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("۲. پردازش سیستمی", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7DD3FC))
                                Text("استعلام مراجع", fontSize = 8.sp, color = Color(0xFFBAE6FD))
                            }
                        }
                        // 3. Green
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF34D399)))
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("۳. صدور و تایید", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF86EFAC))
                                Text("ارسال پیامک", fontSize = 8.sp, color = Color(0xFFA7F3D0))
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Service Hub Section (Only Insurance, Inquiries, Fuel Card) ---
        item {
            Text(
                text = "انتخاب و ثبت خدمات خودرو:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // A) Insurance Service Card (Primary - Dark Theme)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showInsuranceDialog = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "صدور و تمدید انواع بیمه‌نامه خودرو (شخص ثالث و بدنه)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "شخص ثالث، بدنه، حوادث راننده و موتور با انتخاب شرکت و تخفیف عدم خسارت",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Button(
                        onClick = { showInsuranceDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_insurance_form_button")
                    ) {
                        Text("فرم بیمه", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // B) Redesigned Vehicle Inquiries & Tolls Grid (8 Complete Categories)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "خدمات و استعلام‌های هوشمند خودرو و راننده:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Row 1: Fines & Negative Points
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceQuickButton(
                        title = "استعلام خلافی خودرو و موتور",
                        subtitle = "جرائم رانندگی و تسویه راهور",
                        icon = Icons.Default.ReceiptLong,
                        color = Color(0xFFEF4444),
                        onClick = {
                            selectedInquiryType = "استعلام خلافی خودرو و موتورسیکلت"
                            showInquiryDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ServiceQuickButton(
                        title = "استعلام نمره منفی گواهی‌نامه",
                        subtitle = "سوابق نمره منفی راننده",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFE11D48),
                        onClick = {
                            selectedInquiryType = "استعلام نمره منفی گواهی‌نامه"
                            showInquiryDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Driving License Status & Technical Inspection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceQuickButton(
                        title = "استعلام گواهی‌نامه و سوابق",
                        subtitle = "وضعیت اعتبار و کارت رانندگی",
                        icon = Icons.Default.Badge,
                        color = Color(0xFF0284C7),
                        onClick = {
                            selectedInquiryType = "استعلام گواهی‌نامه و سوابق رانندگی"
                            showInquiryDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ServiceQuickButton(
                        title = "استعلام معاینه فنی خودرو",
                        subtitle = "اعتبار گواهی معاینه فنی",
                        icon = Icons.Default.Verified,
                        color = Color(0xFF10B981),
                        onClick = {
                            selectedInquiryType = "استعلام معاینه فنی خودرو"
                            showInquiryDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Vehicle Documents & Highway Tolls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceQuickButton(
                        title = "اسناد خودرو و وضعیت پلاک",
                        subtitle = "سند، پلاک فعال و مالکیت",
                        icon = Icons.Default.FolderSpecial,
                        color = Color(0xFF06B6D4),
                        onClick = {
                            selectedInquiryType = "استعلام اسناد خودرو و وضعیت پلاک"
                            showInquiryDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ServiceQuickButton(
                        title = "عوارض آزادراهی و بزرگراهی",
                        subtitle = "تسویه آنی تردد جاده‌ای",
                        icon = Icons.Default.AltRoute,
                        color = Color(0xFFF59E0B),
                        onClick = {
                            selectedInquiryType = "استعلام عوارض آزادراهی"
                            showInquiryDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 4: Annual Tolls & Vehicle Transfer Tax
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceQuickButton(
                        title = "عوارض سالیانه شهرداری",
                        subtitle = "عوارض خودرو و نوسازی",
                        icon = Icons.Default.LocationCity,
                        color = Color(0xFF059669),
                        onClick = {
                            selectedInquiryType = "استعلام عوارض سالیانه شهرداری"
                            showInquiryDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ServiceQuickButton(
                        title = "مالیات نقل و انتقال خودرو",
                        subtitle = "سامانه etax و تعویض پلاک",
                        icon = Icons.Default.Payments,
                        color = Color(0xFF8B5CF6),
                        onClick = {
                            selectedInquiryType = "استعلام مالیات نقل و انتقال خودرو"
                            showInquiryDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // C) Fuel Card Service Button
                ServiceQuickButton(
                    title = "درخواست و پیگیری کارت سوخت",
                    subtitle = "صدور کارت سوخت نو یا المثنی با ثبت کدپستی و آدرس محل سکونت",
                    icon = Icons.Default.LocalGasStation,
                    color = Color(0xFF0369A1),
                    onClick = { showFuelCardDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- 3. Filter Chips for Submitted Requests ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "پیگیری درخواست‌های ثبت‌شده:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredRequests.size} مورد",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf("همه", "بیمه", "استعلام و عوارض", "کارت سوخت", "در انتظار تایید", "تایید شده")
                items(filters) { f ->
                    FilterChip(
                        selected = selectedFilterCategory == f,
                        onClick = { selectedFilterCategory = f },
                        label = { Text(f, fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // --- 4. Requests List Items ---
        if (filteredRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "موردی در این دسته‌بندی یافت نشد",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "برای شروع، یکی از خدمات بیمه، استعلام یا کارت سوخت را در بالا انتخاب و ثبت نمایید.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredRequests, key = { it.id }) { req ->
                RequestCardItem(
                    request = req
                )
            }
        }
    }

    // --- Dialog 1: Complete Insurance Form ---
    if (showInsuranceDialog) {
        InsuranceFormDialog(
            vehicles = vehicles,
            defaultVehicle = defaultVehicle,
            isSubmitting = isSubmitting,
            onDismiss = { showInsuranceDialog = false },
            onSubmit = { category, company, duration, discount, name, nationalCode, phone, plate, vin, barcode, postalCode, address, details ->
                baleViewModel.submitServiceRequest(
                    requestType = "بیمه - $category",
                    title = "درخواست $category ($company)",
                    fullName = name,
                    nationalCode = nationalCode,
                    phoneNumber = phone,
                    vehiclePlate = plate,
                    vinCode = vin,
                    barcode = barcode,
                    postalCode = postalCode,
                    address = address,
                    insuranceCategory = category,
                    insuranceCompany = company,
                    durationMonths = duration,
                    discountPercent = discount,
                    details = details
                )
                showInsuranceDialog = false
            }
        )
    }

    // --- Dialog 2: Comprehensive Vehicle Inquiry Form ---
    if (showInquiryDialog) {
        ComprehensiveInquiryDialog(
            inquiryType = selectedInquiryType,
            vehicles = vehicles,
            defaultVehicle = defaultVehicle,
            isSubmitting = isSubmitting,
            onDismiss = { showInquiryDialog = false },
            onSubmit = { type, name, nationalCode, phone, plate, vin, barcode, engine, chassis, postalCode, address, details ->
                baleViewModel.submitServiceRequest(
                    requestType = type,
                    title = type,
                    fullName = name,
                    nationalCode = nationalCode,
                    phoneNumber = phone,
                    vehiclePlate = plate,
                    vinCode = vin,
                    barcode = barcode,
                    engineNumber = engine,
                    chassisNumber = chassis,
                    postalCode = postalCode,
                    address = address,
                    details = details
                )
                showInquiryDialog = false
            }
        )
    }

    // --- Dialog 3: Fuel Card Request Form ---
    if (showFuelCardDialog) {
        FuelCardFormDialog(
            vehicles = vehicles,
            defaultVehicle = defaultVehicle,
            isSubmitting = isSubmitting,
            onDismiss = { showFuelCardDialog = false },
            onSubmit = { cardType, name, nationalCode, phone, plate, vin, barcode, engine, chassis, postalCode, address, details ->
                baleViewModel.submitServiceRequest(
                    requestType = "کارت سوخت - $cardType",
                    title = "درخواست صدور کارت سوخت ($cardType)",
                    fullName = name,
                    nationalCode = nationalCode,
                    phoneNumber = phone,
                    vehiclePlate = plate,
                    vinCode = vin,
                    barcode = barcode,
                    engineNumber = engine,
                    chassisNumber = chassis,
                    postalCode = postalCode,
                    address = address,
                    details = details
                )
                showFuelCardDialog = false
            }
        )
    }
}

@Composable
private fun ServiceQuickButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(82.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.5.sp,
                    maxLines = 2,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun RequestCardItem(
    request: ServiceRequestEntity
) {
    val context = LocalContext.current

    val isPending = request.status == ServiceRequestEntity.STATUS_PENDING
    val isApproved = request.status == ServiceRequestEntity.STATUS_APPROVED
    val isRejected = request.status == ServiceRequestEntity.STATUS_REJECTED

    // Dynamic rotating ticker messages for pending state
    var currentMessageIndex by remember { mutableIntStateOf(0) }
    val waitingMessages = remember {
        listOf(
            "⏳ در حال بررسی و تطبیق اطلاعات توسط کارشناس و مدیر...",
            "📡 اطلاعات برای کارشناس ارشد ارسال شد؛ منتظر بررسی...",
            "👤 کارشناس به زودی نتیجه بررسی مدارک را ثبت خواهد نمود.",
            "⚡ وضعیت با تایید مدیریت سامانه بلافاصله بروزرسانی و رنگی خواهد شد."
        )
    }

    LaunchedEffect(isPending) {
        if (isPending) {
            while (true) {
                delay(5500)
                currentMessageIndex = (currentMessageIndex + 1) % waitingMessages.size
            }
        }
    }

    // 3 distinct colors & status themes
    val (targetColor, targetBg, targetBorder) = when {
        isApproved -> Triple(Color(0xFF059669), Color(0xFFD1FAE5), Color(0xFF10B981).copy(alpha = 0.5f))
        isPending -> Triple(Color(0xFFEA580C), Color(0xFFFFF7ED), Color(0xFFFB923C).copy(alpha = 0.6f))
        else -> Triple(Color(0xFFDC2626), Color(0xFFFEE2E2), Color(0xFFEF4444).copy(alpha = 0.5f))
    }

    val statusColor by animateColorAsState(targetColor, animationSpec = tween(1200, easing = FastOutSlowInEasing), label = "statusColor")
    val statusBg by animateColorAsState(targetBg, animationSpec = tween(1200, easing = FastOutSlowInEasing), label = "statusBg")
    val statusBorder by animateColorAsState(targetBorder, animationSpec = tween(1200, easing = FastOutSlowInEasing), label = "statusBorder")

    val statusTitle = when {
        isApproved -> "تایید شده توسط کارشناس و مدیر ✅"
        isPending -> "در حال بررسی توسط کارشناس ⏳"
        else -> "عدم تایید مدارک ❌"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.2.dp, statusBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title, Status Badge & In-app Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val icon = when {
                        request.requestType.contains("بیمه") -> Icons.Default.Security
                        request.requestType.contains("کارت سوخت") -> Icons.Default.LocalGasStation
                        else -> Icons.Default.ReceiptLong
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = request.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                        Text(
                            text = request.requestType,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.5.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusBg,
                        border = BorderStroke(0.8.dp, statusColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = statusTitle,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                }
            }

            // Dynamic 3-Color Status Banner & Waiting Message Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusBg.copy(alpha = 0.6f),
                border = BorderStroke(0.8.dp, statusBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        when {
                            isApproved -> Icons.Default.CheckCircle
                            isPending -> Icons.Default.HourglassTop
                            else -> Icons.Default.Cancel
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    AnimatedContent(
                        targetState = when {
                            isApproved -> "درخواست شما توسط کارشناس و مدیر با موفقیت تایید و نهایی شد."
                            isPending -> waitingMessages[currentMessageIndex]
                            else -> "درخواست توسط کارشناس و مدیر رد شد یا نیاز به بازبینی مدارک دارد."
                        },
                        transitionSpec = {
                            fadeIn(tween(700)).togetherWith(fadeOut(tween(350)))
                        },
                        label = "waitingMessageAnim"
                    ) { msg ->
                        Text(
                            text = msg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Summary Information Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("متقاضی: ${request.fullName}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        Text("پلاک: ${request.vehiclePlate}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("کد ملی: ${request.nationalCode}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFBAE6FD))
                        Text("تماس: ${request.phoneNumber}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }

                    if (request.vinCode.isNotBlank() || request.barcodeNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (request.vinCode.isNotBlank()) Text("VIN: ${request.vinCode}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            if (request.barcodeNumber.isNotBlank()) Text("بارکد: ${request.barcodeNumber}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    if (request.engineNumber.isNotBlank() || request.chassisNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (request.engineNumber.isNotBlank()) Text("موتور: ${request.engineNumber}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            if (request.chassisNumber.isNotBlank()) Text("شاسی: ${request.chassisNumber}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    if (request.insuranceCompany.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نمایندگی: ${request.insuranceCompany}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            Text("مدت: ${request.durationMonths} ماه | تخفیف: ${request.discountPercent}٪", fontSize = 10.sp, color = Color(0xFF38BDF8))
                        }
                    }

                    if (request.postalCode.isNotBlank() || request.address.isNotBlank()) {
                        Text(
                            text = "آدرس و کدپستی: ${request.address} (کد پستی: ${request.postalCode})",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    if (request.additionalDetails.isNotBlank()) {
                        Text(
                            text = "توضیحات تکمیلی: ${request.additionalDetails}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFDE68A)
                        )
                    }
                }
            }

            // Clean Footer: Tracking Code, Date & Bot Command Guide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "کد پیگیری: #REQ_${request.id}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "ثبت: ${PersianDateHelper.toPersianDateTime(request.submissionDateMillis)}",
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (isPending) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(0.6.dp, Color(0xFFEA580C).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "در انتظار بررسی کارشناس",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFB923C),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Text(
                        text = if (isApproved) "تایید نهایی شده" else "نیاز به بازبینی",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }
        }
    }
}

// =======================================================================================
// Dialog 1: COMPLETE INSURANCE FORM WITH CATEGORIZED SELECTION
// =======================================================================================
@Composable
private fun InsuranceFormDialog(
    vehicles: List<VehicleEntity>,
    defaultVehicle: VehicleEntity?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (category: String, company: String, duration: Int, discount: Int, name: String, nationalCode: String, phone: String, plate: String, vin: String, barcode: String, postalCode: String, address: String, details: String) -> Unit
) {
    val context = LocalContext.current
    val categories = listOf(
        "بیمه شخص ثالث",
        "بیمه بدنه خودرو",
        "بیمه موتورسیکلت"
    )

    var selectedCategory by remember { mutableStateOf(if (defaultVehicle?.isMotorcycle == true) categories[2] else categories[0]) }
    var selectedCompany by remember { mutableStateOf(InsuranceAgencies.DEFAULT_AGENCY) }

    var fullName by remember { mutableStateOf("") }
    var nationalCode by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var vehiclePlate by remember { mutableStateOf(defaultVehicle?.formattedPlate.orEmpty()) }
    var details by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
        ) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(10.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .widthIn(max = 520.dp)
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header with Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0F172A), Color(0xFF0284C7))
                                )
                            )
                            .padding(18.dp)
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
                                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                }
                                Column {
                                    Text(
                                        text = "فرم درخواست و تمدید بیمه‌نامه",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.5.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "شخص ثالث، بدنه، حوادث راننده و موتورسیکلت",
                                        fontSize = 11.5.sp,
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
                                Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                // 1. Category Selection
                item {
                    Text("نوع بیمه:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. Vehicle Selection / Plate
                item {
                    Text("وسیله نقلیه و پلاک:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (vehicles.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            items(vehicles) { v ->
                                FilterChip(
                                    selected = vehiclePlate == v.formattedPlate,
                                    onClick = {
                                        vehiclePlate = v.formattedPlate
                                        if (v.isMotorcycle) {
                                            selectedCategory = "بیمه موتورسیکلت"
                                        }
                                    },
                                    label = { Text("${when { v.isMotorcycle -> "🏍️"; v.isArvand -> "🌴"; else -> "🚗" }} ${v.title}", fontSize = 11.5.sp) }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = vehiclePlate,
                        onValueChange = { vehiclePlate = it },
                        label = { Text("شماره پلاک (ملی، اروندی یا موتورسیکلت)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // 3. Complete Themed Insurance Agency Picker (26 Iranian Agencies & Branches)
                item {
                    InsuranceAgencyPickerField(
                        selectedAgency = selectedCompany,
                        onAgencySelected = { selectedCompany = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = "انتخاب شرکت یا نمایندگی بیمه‌گر:"
                    )
                }

                // 4. Personal Info (National Code is Mandatory)
                item {
                    Text("مشخصات متقاضی:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("نام و نام خانوادگی بیمه‌گزار") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 11) phoneNumber = it },
                            label = { Text("شماره تماس") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = nationalCode,
                            onValueChange = { if (it.length <= 10) nationalCode = it },
                            label = { Text("کد ملی (اجباری - ۱۰ رقم) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // 5. Notes / Details (Mandatory)
                item {
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("توضیحات و مشخصات تکمیلی (اجباری) *") },
                        placeholder = { Text("مدل، سال ساخت، نوع کاربری، سوابق تخفیف بیمه‌نامه قبلی یا اطلاعات بیمه‌گر...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Buttons
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (vehiclePlate.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("لطفاً شماره پلاک را وارد کنید")
                                    return@Button
                                }
                                if (nationalCode.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وارد کردن کد ملی متقاضی الزامی است")
                                    return@Button
                                }
                                if (details.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وارد کردن توضیحات تکمیلی الزامی است")
                                    return@Button
                                }
                                if (fullName.isNotBlank() && phoneNumber.isNotBlank()) {
                                    onSubmit(
                                        selectedCategory,
                                        selectedCompany,
                                        12,
                                        0,
                                        fullName.trim(),
                                        nationalCode.trim(),
                                        phoneNumber.trim(),
                                        vehiclePlate.trim(),
                                        "",
                                        "",
                                        "",
                                        "",
                                        details.trim()
                                    )
                                }
                            },
                            enabled = !isSubmitting && fullName.isNotBlank() && phoneNumber.isNotBlank() && nationalCode.isNotBlank() && details.isNotBlank() && vehiclePlate.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            } else {
                                Text("ثبت و ارسال مستقیم درخواست")
                            }
                        }
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
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
}

// =======================================================================================
// Dialog 2: COMPREHENSIVE INQUIRY FORM (ALL VEHICLE INFO, POSTAL, ADDRESS, PERSONAL INFO)
// =======================================================================================
@Composable
private fun ComprehensiveInquiryDialog(
    inquiryType: String,
    vehicles: List<VehicleEntity>,
    defaultVehicle: VehicleEntity?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (type: String, name: String, nationalCode: String, phone: String, plate: String, vin: String, barcode: String, engine: String, chassis: String, postalCode: String, address: String, details: String) -> Unit
) {
    val context = LocalContext.current
    val inquiryTypes = listOf(
        "استعلام خلافی خودرو و موتورسیکلت",
        "استعلام نمره منفی گواهی‌نامه",
        "استعلام گواهی‌نامه و سوابق رانندگی",
        "استعلام معاینه فنی خودرو",
        "استعلام اسناد خودرو و وضعیت پلاک",
        "استعلام عوارض آزادراهی",
        "استعلام عوارض سالیانه شهرداری",
        "استعلام مالیات نقل و انتقال خودرو"
    )

    var selectedType by remember { mutableStateOf(inquiryType) }

    var fullName by remember { mutableStateOf("") }
    var nationalCode by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var vehiclePlate by remember { mutableStateOf(defaultVehicle?.formattedPlate.orEmpty()) }
    var vinCode by remember { mutableStateOf("") }
    var barcodeNumber by remember { mutableStateOf("") }
    var engineNumber by remember { mutableStateOf("") }
    var chassisNumber by remember { mutableStateOf("") }

    var postalCode by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
        ) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(10.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .widthIn(max = 520.dp)
                    .padding(vertical = 12.dp)
            ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Gradient Header matching App Theme
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF0369A1),
                                    Color(0xFF0284C7)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "فرم جامع استعلام، عوارض و خلافی",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.5.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "خلافی، عوارض آزادراهی، سالیانه و مالیات با صدور مفاصاحساب",
                                    fontSize = 11.5.sp,
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
                            Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                // 1. Inquiry Type Chips
                item {
                    Text("نوع استعلام مورد نظر:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        inquiryTypes.forEach { t ->
                            FilterChip(
                                selected = selectedType == t,
                                onClick = { selectedType = t },
                                label = { Text(t, fontSize = 11.sp, fontWeight = if (selectedType == t) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 2. Personal Information (Only necessary fields)
                item {
                    Text("اطلاعات متقاضی / مالک:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("نام و نام خانوادگی متقاضی") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nationalCode,
                            onValueChange = { if (it.length <= 10) nationalCode = it },
                            label = { Text("کد ملی متقاضی (اجباری - ۱۰ رقم) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 11) phoneNumber = it },
                            label = { Text("شماره همراه") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // 3. Vehicle Specific Fields (Only fields required for the selected inquiry)
                item {
                    Text("اطلاعات خودروی مورد استعلام:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = vehiclePlate,
                        onValueChange = { vehiclePlate = it },
                        label = { Text("شماره پلاک خودرو یا موتور") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (selectedType.contains("خلافی") || selectedType.contains("سالیانه")) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = vinCode,
                                onValueChange = { if (it.length <= 17) vinCode = it },
                                label = { Text("شماره VIN شناسایی (۱۷ کاراکتر)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            if (selectedType.contains("خلافی")) {
                                OutlinedTextField(
                                    value = barcodeNumber,
                                    onValueChange = { barcodeNumber = it },
                                    label = { Text("بارکد کارت خودرو (۸-۹ رقم)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                if (selectedType.contains("مالیات")) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = engineNumber,
                                onValueChange = { engineNumber = it },
                                label = { Text("شماره موتور") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = chassisNumber,
                                onValueChange = { chassisNumber = it },
                                label = { Text("شماره شاسی") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }

                // 4. Postal Code & Address
                item {
                    Text("کدپستی و آدرس محل سکونت مالک:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { if (it.length <= 10) postalCode = it },
                        label = { Text("کد پستی ۱۰ رقمی محل سکونت") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("آدرس دقیق پستی محل سکونت") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                item {
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("توضیحات تکمیلی استعلام (اجباری) *") },
                        placeholder = { Text("توضیحات لازم پیرامون استعلام یا سال ساخت...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                // Action Buttons
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (nationalCode.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وارد کردن کد ملی الزامی است")
                                    return@Button
                                }
                                if (details.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وارد کردن توضیحات الزامی است")
                                    return@Button
                                }
                                if (fullName.isNotBlank() && phoneNumber.isNotBlank() && vehiclePlate.isNotBlank()) {
                                    onSubmit(
                                        selectedType,
                                        fullName.trim(),
                                        nationalCode.trim(),
                                        phoneNumber.trim(),
                                        vehiclePlate.trim(),
                                        vinCode.trim(),
                                        barcodeNumber.trim(),
                                        engineNumber.trim(),
                                        chassisNumber.trim(),
                                        postalCode.trim(),
                                        address.trim(),
                                        details.trim()
                                    )
                                }
                            },
                            enabled = !isSubmitting && fullName.isNotBlank() && phoneNumber.isNotBlank() && nationalCode.isNotBlank() && details.isNotBlank() && vehiclePlate.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            } else {
                                Text("ثبت و ارسال استعلام")
                            }
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
}
}
}

// =======================================================================================
// Dialog 3: COMPREHENSIVE FUEL CARD REQUEST FORM
// =======================================================================================
@Composable
private fun FuelCardFormDialog(
    vehicles: List<VehicleEntity>,
    defaultVehicle: VehicleEntity?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (cardType: String, name: String, nationalCode: String, phone: String, plate: String, vin: String, barcode: String, engine: String, chassis: String, postalCode: String, address: String, details: String) -> Unit
) {
    val context = LocalContext.current
    val cardTypes = listOf(
        "صدور کارت سوخت المثنی (مفقودی / آسیب‌دیده)",
        "درخواست کارت سوخت خودرو نو شماره‌گذاری"
    )

    var selectedCardType by remember { mutableStateOf(cardTypes[0]) }

    var fullName by remember { mutableStateOf("") }
    var nationalCode by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var vehiclePlate by remember { mutableStateOf(defaultVehicle?.formattedPlate.orEmpty()) }
    var vinCode by remember { mutableStateOf("") }
    var barcodeNumber by remember { mutableStateOf("") }
    var engineNumber by remember { mutableStateOf("") }
    var chassisNumber by remember { mutableStateOf("") }

    var postalCode by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
        ) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(10.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .widthIn(max = 520.dp)
                    .padding(vertical = 12.dp)
            ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Gradient Header matching App Theme
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF0369A1),
                                    Color(0xFF0284C7)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.LocalGasStation,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "فرم درخواست و صدور کارت سوخت",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.5.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "پیگیری المثنی، نو شماره و توزیع مرسوله پستی",
                                    fontSize = 11.5.sp,
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
                            Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                // 1. Card Type
                item {
                    Text("نوع درخواست کارت سوخت:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        cardTypes.forEach { ct ->
                            FilterChip(
                                selected = selectedCardType == ct,
                                onClick = { selectedCardType = ct },
                                label = { Text(ct, fontSize = 11.sp, fontWeight = if (selectedCardType == ct) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 2. Personal Info
                item {
                    Text("مشخصات فردی متقاضی / مالک:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("نام و نام خانوادگی مالک خودرو") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nationalCode,
                            onValueChange = { if (it.length <= 10) nationalCode = it },
                            label = { Text("کد ملی متقاضی (اجباری - ۱۰ رقم) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 11) phoneNumber = it },
                            label = { Text("شماره همراه فعال") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // 3. Vehicle Info
                item {
                    Text("کل اطلاعات خودرو:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = vehiclePlate,
                        onValueChange = { vehiclePlate = it },
                        label = { Text("شماره پلاک خودرو (ملی یا اروندی)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = vinCode,
                            onValueChange = { if (it.length <= 17) vinCode = it },
                            label = { Text("کد VIN (۱۷ رقم)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = barcodeNumber,
                            onValueChange = { barcodeNumber = it },
                            label = { Text("بارکد پستی پشت کارت") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = engineNumber,
                            onValueChange = { engineNumber = it },
                            label = { Text("شماره موتور") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = chassisNumber,
                            onValueChange = { chassisNumber = it },
                            label = { Text("شماره شاسی") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // 4. Postal Code & Address for Delivery
                item {
                    Text("کد پستی و آدرس پستی جهت ارسال کارت:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { if (it.length <= 10) postalCode = it },
                        label = { Text("کد پستی ۱۰ رقمی مقصد") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("آدرس دقیق پستی جهت تحویل توسط پستچی") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                item {
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("توضیحات تکمیلی درخواست (اجباری) *") },
                        placeholder = { Text("توضیحات مربوط به مفقودی، المثنی یا نو شماره...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                // Submit Buttons
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (nationalCode.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وارد کردن کد ملی الزامی است")
                                    return@Button
                                }
                                if (details.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وارد کردن توضیحات تکمیلی الزامی است")
                                    return@Button
                                }
                                if (fullName.isNotBlank() && phoneNumber.isNotBlank() && vehiclePlate.isNotBlank()) {
                                    onSubmit(
                                        selectedCardType,
                                        fullName.trim(),
                                        nationalCode.trim(),
                                        phoneNumber.trim(),
                                        vehiclePlate.trim(),
                                        vinCode.trim(),
                                        barcodeNumber.trim(),
                                        engineNumber.trim(),
                                        chassisNumber.trim(),
                                        postalCode.trim(),
                                        address.trim(),
                                        details.trim()
                                    )
                                }
                            },
                            enabled = !isSubmitting && fullName.isNotBlank() && phoneNumber.isNotBlank() && nationalCode.isNotBlank() && details.isNotBlank() && vehiclePlate.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            } else {
                                Text("ثبت درخواست کارت سوخت")
                            }
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
}
}
}
