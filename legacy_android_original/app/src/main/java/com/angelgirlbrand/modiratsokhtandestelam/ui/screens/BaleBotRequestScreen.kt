package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceRequestEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.IranianPlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel
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

    LaunchedEffect(submissionMessage) {
        if (submissionMessage != null) {
            Toast.makeText(context, submissionMessage, Toast.LENGTH_LONG).show()
            baleViewModel.clearMessage()
        }
    }

    val defaultVehicle = vehicles.firstOrNull()

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

        // --- 2. Service Hub Section (Only Insurance, Inquiries, Fuel Card) ---
        item {
            Text(
                text = "انتخاب و ثبت خدمات خودرو:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // A) Insurance Service Card (Primary)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showInsuranceDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7).copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.25f))
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
                                text = "صدور و تمدید انواع بیمه‌نامه خودرو",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "شخص ثالث، بدنه، حوادث راننده و موتور با انتخاب شرکت و تخفیف عدم خسارت",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { showInsuranceDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_insurance_form_button")
                    ) {
                        Text("فرم کامل بیمه", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // B) Vehicle Inquiries & Tolls Grid (خلافی، عوارض آزادراهی، عوارض سالیانه، مالیات نقل و انتقال)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceQuickButton(
                        title = "استعلام خلافی خودرو",
                        subtitle = "جرائم رانندگی راهور",
                        icon = Icons.Default.ReceiptLong,
                        color = Color(0xFFEF4444),
                        onClick = {
                            selectedInquiryType = "استعلام خلافی خودرو"
                            showInquiryDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ServiceQuickButton(
                        title = "عوارض آزادراهی",
                        subtitle = "تسویه آنی تردد بزرگراهی",
                        icon = Icons.Default.AltRoute,
                        color = Color(0xFFF59E0B),
                        onClick = {
                            selectedInquiryType = "استعلام عوارض آزادراهی"
                            showInquiryDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceQuickButton(
                        title = "عوارض سالیانه خودرو",
                        subtitle = "شهرداری و نوسازی",
                        icon = Icons.Default.LocationCity,
                        color = Color(0xFF10B981),
                        onClick = {
                            selectedInquiryType = "استعلام عوارض سالیانه خودرو"
                            showInquiryDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ServiceQuickButton(
                        title = "مالیات نقل و انتقال",
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
            items(filteredRequests) { req ->
                RequestCardItem(
                    request = req,
                    onApprove = { baleViewModel.approveRequest(req.id) },
                    onDelete = { baleViewModel.deleteRequest(req) }
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
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RequestCardItem(
    request: ServiceRequestEntity,
    onApprove: () -> Unit,
    onDelete: () -> Unit
) {
    val isPending = request.status == ServiceRequestEntity.STATUS_PENDING
    val isApproved = request.status == ServiceRequestEntity.STATUS_APPROVED

    val statusColor = when {
        isApproved -> Color(0xFF10B981)
        isPending -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    val statusBg = when {
        isApproved -> Color(0xFFD1FAE5)
        isPending -> Color(0xFFFEF3C7)
        else -> Color(0xFFFEE2E2)
    }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title & Status Badge
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = request.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = request.requestType,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = request.status,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Summary Information Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("متقاضی: ${request.fullName}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text("پلاک: ${request.vehiclePlate}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("کد ملی: ${request.nationalCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("تماس: ${request.phoneNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (request.vinCode.isNotBlank() || request.barcodeNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (request.vinCode.isNotBlank()) Text("VIN: ${request.vinCode}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (request.barcodeNumber.isNotBlank()) Text("بارکد: ${request.barcodeNumber}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (request.engineNumber.isNotBlank() || request.chassisNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (request.engineNumber.isNotBlank()) Text("موتور: ${request.engineNumber}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (request.chassisNumber.isNotBlank()) Text("شاسی: ${request.chassisNumber}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (request.insuranceCompany.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("بیمه‌گر: ${request.insuranceCompany}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("مدت: ${request.durationMonths} ماه | تخفیف: ${request.discountPercent}٪", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (request.postalCode.isNotBlank() || request.address.isNotBlank()) {
                        Text(
                            text = "آدرس و کدپستی: ${request.address} (کد پستی: ${request.postalCode})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (request.additionalDetails.isNotBlank()) {
                        Text(
                            text = "توضیحات: ${request.additionalDetails}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Footer: Tracking Code & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "کد پیگیری: ${request.baleMessageId.take(12)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = dateFormat.format(Date(request.submissionDateMillis)),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isPending) {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("تایید درخواست", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "حذف رکورد",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
    val categories = listOf(
        "بیمه شخص ثالث (اجباری)",
        "بیمه بدنه خودرو (جامع)",
        "بیمه حوادث راننده",
        "بیمه موتور سیکلت"
    )

    val companies = listOf(
        "بیمه ایران", "بیمه آسیا", "بیمه دانا", "بیمه البرز",
        "بیمه پاسارگاد", "بیمه پارسیان", "بیمه رازی", "بیمه کوثر", "بیمه معلم"
    )

    val durationOptions = listOf(
        "۱ ماهه" to 1,
        "۳ ماهه" to 3,
        "۶ ماهه" to 6,
        "۱ ساله" to 12
    )

    val discountOptions = listOf(0, 10, 20, 30, 40, 50, 60, 70)

    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var selectedCompany by remember { mutableStateOf(companies[0]) }
    var selectedDurationMonths by remember { mutableStateOf(12) }
    var selectedDiscount by remember { mutableStateOf(10) }

    var fullName by remember { mutableStateOf("") }
    var nationalCode by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var vehiclePlate by remember { mutableStateOf(defaultVehicle?.formattedPlate.orEmpty()) }
    var vinCode by remember { mutableStateOf("") }
    var barcodeNumber by remember { mutableStateOf("") }

    var postalCode by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "فرم کامل صدور و تمدید بیمه‌نامه خودرو",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "انتخاب دسته‌بندی، شرکت بیمه‌گر، تخفیف، مشخصات خودرو و ارسال پستی",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 1. Category Selection
                item {
                    Text("۱. دسته‌بندی و نوع بیمه:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 2. Insurance Company Selection
                item {
                    Text("۲. شرکت بیمه‌گر منتخب:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(companies) { c ->
                            FilterChip(
                                selected = selectedCompany == c,
                                onClick = { selectedCompany = c },
                                label = { Text(c, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 3. Duration & Discount
                item {
                    Text("۳. مدت اعتبار بیمه‌نامه:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        durationOptions.forEach { (label, months) ->
                            FilterChip(
                                selected = selectedDurationMonths == months,
                                onClick = { selectedDurationMonths = months },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Text("۴. تخفیف عدم خسارت:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(discountOptions) { d ->
                            FilterChip(
                                selected = selectedDiscount == d,
                                onClick = { selectedDiscount = d },
                                label = { Text(if (d == 0) "بدون تخفیف" else "$d٪", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 5. Personal Info
                item {
                    Text("۵. مشخصات متقاضی (اطلاعات فردی):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("نام و نام خانوادگی بیمه‌گزار") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nationalCode,
                            onValueChange = { if (it.length <= 10) nationalCode = it },
                            label = { Text("کد ملی (۱۰ رقم)") },
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

                // 6. Vehicle Info
                item {
                    Text("۶. مشخصات کامل خودرو:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = vehiclePlate,
                        onValueChange = { vehiclePlate = it },
                        label = { Text("شماره پلاک خودرو") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = vinCode,
                            onValueChange = { if (it.length <= 17) vinCode = it },
                            label = { Text("شماره VIN (۱۷ رقم)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = barcodeNumber,
                            onValueChange = { barcodeNumber = it },
                            label = { Text("بارکد کارت خودرو") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // 7. Postal Code & Address
                item {
                    Text("۷. کدپستی و آدرس محل سکونت (ارسال مدارک):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { if (it.length <= 10) postalCode = it },
                        label = { Text("کد پستی ۱۰ رقمی") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("آدرس دقیق محل سکونت / تحویل پستی") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                item {
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("شماره بیمه‌نامه قبلی یا توضیحات تکمیلی") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
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
                                if (fullName.isNotBlank() && phoneNumber.isNotBlank() && vehiclePlate.isNotBlank()) {
                                    onSubmit(
                                        selectedCategory,
                                        selectedCompany,
                                        selectedDurationMonths,
                                        selectedDiscount,
                                        fullName,
                                        nationalCode.ifBlank { "کد ملی ثبت نشده" },
                                        phoneNumber,
                                        vehiclePlate,
                                        vinCode,
                                        barcodeNumber,
                                        postalCode,
                                        address,
                                        details
                                    )
                                }
                            },
                            enabled = !isSubmitting,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            } else {
                                Text("ثبت و ارسال درخواست بیمه")
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
    val inquiryTypes = listOf(
        "استعلام خلافی خودرو",
        "استعلام عوارض آزادراهی",
        "استعلام عوارض سالیانه خودرو",
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "فرم جامع استعلام و تسویه عوارض و خلافی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "ثبت مشخصات کامل خودرو، اطلاعات فردی و آدرس محل سکونت جهت صدور مفاصاحساب رسمی",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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

                // 2. Personal Information
                item {
                    Text("اطلاعات فردی مالک / متقاضی:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("نام و نام خانوادگی مالک") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nationalCode,
                            onValueChange = { if (it.length <= 10) nationalCode = it },
                            label = { Text("کد ملی مالک (۱۰ رقم)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 11) phoneNumber = it },
                            label = { Text("شماره همراه مالک") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // 3. Complete Vehicle Information
                item {
                    Text("کل اطلاعات و مشخصات ماشین:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = vehiclePlate,
                        onValueChange = { vehiclePlate = it },
                        label = { Text("شماره پلاک ملی خودرو") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = vinCode,
                            onValueChange = { if (it.length <= 17) vinCode = it },
                            label = { Text("شماره VIN شناسایی") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = barcodeNumber,
                            onValueChange = { barcodeNumber = it },
                            label = { Text("بارکد کارت خودرو (۸-۹ رقم)") },
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
                        label = { Text("توضیحات تکمیلی (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
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
                                if (fullName.isNotBlank() && phoneNumber.isNotBlank() && vehiclePlate.isNotBlank()) {
                                    onSubmit(
                                        selectedType,
                                        fullName,
                                        nationalCode.ifBlank { "کد ملی ثبت نشده" },
                                        phoneNumber,
                                        vehiclePlate,
                                        vinCode,
                                        barcodeNumber,
                                        engineNumber,
                                        chassisNumber,
                                        postalCode,
                                        address,
                                        details
                                    )
                                }
                            },
                            enabled = !isSubmitting,
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "فرم درخواست و صدور کارت سوخت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "ثبت اطلاعات مالک، مشخصات فنی خودرو و آدرس پستی جهت صدور و تحویل مرسوله کارت سوخت",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
                            label = { Text("کد ملی مالک (۱۰ رقم)") },
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
                        label = { Text("شماره پلاک انتظامی خودرو") },
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
                        label = { Text("توضیحات تکمیلی (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
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
                                if (fullName.isNotBlank() && phoneNumber.isNotBlank() && vehiclePlate.isNotBlank()) {
                                    onSubmit(
                                        selectedCardType,
                                        fullName,
                                        nationalCode.ifBlank { "کد ملی ثبت نشده" },
                                        phoneNumber,
                                        vehiclePlate,
                                        vinCode,
                                        barcodeNumber,
                                        engineNumber,
                                        chassisNumber,
                                        postalCode,
                                        address,
                                        details
                                    )
                                }
                            },
                            enabled = !isSubmitting,
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
