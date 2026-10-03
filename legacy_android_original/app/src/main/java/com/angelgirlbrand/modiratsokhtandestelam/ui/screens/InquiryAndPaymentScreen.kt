package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.InquiryRecordEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.IranianPlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InquiryAndPaymentScreen(
    baleViewModel: BaleServiceViewModel,
    fuelViewModel: FuelViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val inquiries by baleViewModel.inquiries.collectAsState()
    val isSubmitting by baleViewModel.isSubmitting.collectAsState()
    val vehicles by fuelViewModel.vehicles.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("همه استعلام‌ها", "خلافی راهور", "عوارض آزادراهی", "عوارض سالیانه", "مالیات نقل و انتقال")

    var showInquiryDialog by remember { mutableStateOf(false) }
    var selectedInquiryForReceipt by remember { mutableStateOf<InquiryRecordEntity?>(null) }

    val filteredInquiries = remember(inquiries, selectedTabIndex) {
        when (selectedTabIndex) {
            1 -> inquiries.filter { it.inquiryType.contains("خلافی") }
            2 -> inquiries.filter { it.inquiryType.contains("آزادراهی") }
            3 -> inquiries.filter { it.inquiryType.contains("سالیانه") || it.inquiryType.contains("شهرداری") }
            4 -> inquiries.filter { it.inquiryType.contains("مالیات") }
            else -> inquiries
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ================= 1. ULTRA-CHIC HERO BANNER =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inquiry_hero_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.25f))
            ) {
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
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF38BDF8).copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "سامانه استعلام خودرو",
                                        color = Color(0xFFE0F2FE),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "استعلام",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "استعلام خلافی راهور، عوارض آزادراهی، عوارض سالیانه و مالیات نقل و انتقال",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFBAE6FD)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // CTA Button
                        Button(
                            onClick = { showInquiryDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("new_inquiry_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF0369A1)
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ثبت استعلام جدید", fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }

        // ================= 2. TAB SELECTOR =================
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tabTitles.indices.toList()) { index ->
                    FilterChip(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        label = { Text(tabTitles[index], fontSize = 11.sp, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // ================= 3. INQUIRIES LIST =================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سوابق استعلام‌های خودرو:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredInquiries.size} مورد",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (filteredInquiries.isEmpty()) {
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
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "استعلامی در این بخش ثبت نشده است",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "جهت استعلام خلافی، عوارض آزادراهی یا مالیات، دکمه «ثبت استعلام جدید» را بزنید.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredInquiries) { inq ->
                InquiryCardItem(
                    inquiry = inq,
                    onApproveByAdmin = {
                        baleViewModel.approveInquiry(inq.id)
                        Toast.makeText(context, "درخواست تایید و تسویه گردید", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = { baleViewModel.deleteInquiry(inq) }
                )
            }
        }
    }

    // ================= DIALOG: COMPREHENSIVE INQUIRY FORM =================
    if (showInquiryDialog) {
        ChicInquiryFormDialog(
            vehicles = vehicles,
            initialType = if (selectedTabIndex in 1..4) tabTitles[selectedTabIndex] else "استعلام خلافی خودرو",
            isSubmitting = isSubmitting,
            onDismiss = { showInquiryDialog = false },
            onSubmit = { type, title, plate, barcode, nationalId, amount, name, phone, vin, engine, chassis, postal, addr, prefMessenger, receiptRef ->
                baleViewModel.submitInquiry(
                    inquiryType = type,
                    title = title,
                    plateNumber = plate,
                    barcodeOrVin = barcode,
                    nationalId = nationalId,
                    amount = amount,
                    workflowMethod = "ADMIN_PAYMENT",
                    fullName = name,
                    phoneNumber = phone,
                    vinCode = vin,
                    barcode = barcode,
                    engineNumber = engine,
                    chassisNumber = chassis,
                    postalCode = postal,
                    address = "$addr (پیام‌رسان جهت ارسال نتیجه: $prefMessenger - فیش واریز: $receiptRef)"
                )
                showInquiryDialog = false
                Toast.makeText(context, "درخواست استعلام ثبت شد و برای بررسی و تسویه نزد مدیر ارسال گردید.", Toast.LENGTH_LONG).show()
            }
        )
    }
}

// =======================================================================================
// COMPONENT: INQUIRY CARD ITEM
// =======================================================================================
@Composable
private fun InquiryCardItem(
    inquiry: InquiryRecordEntity,
    onApproveByAdmin: () -> Unit,
    onDelete: () -> Unit
) {
    val isPaid = inquiry.status.contains("تسویه") || inquiry.status.contains("پرداخت شد")
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
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isPaid) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPaid) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (isPaid) Color(0xFF16A34A) else Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = inquiry.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "پلاک: ${inquiry.plateNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPaid) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (isPaid) "تسویه و انجام شد" else "در حال بررسی و تسویه مدیر",
                        color = if (isPaid) Color(0xFF16A34A) else Color(0xFFB45309),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Inquiry Details Surface
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (inquiry.fullName.isNotBlank() || inquiry.phoneNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (inquiry.fullName.isNotBlank()) Text("مالک: ${inquiry.fullName}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            if (inquiry.phoneNumber.isNotBlank()) Text("تلفن: ${inquiry.phoneNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (inquiry.vinCode.isNotBlank() || inquiry.barcode.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (inquiry.vinCode.isNotBlank()) Text("VIN: ${inquiry.vinCode}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (inquiry.barcode.isNotBlank()) Text("بارکد: ${inquiry.barcode}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (inquiry.engineNumber.isNotBlank() || inquiry.chassisNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (inquiry.engineNumber.isNotBlank()) Text("موتور: ${inquiry.engineNumber}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (inquiry.chassisNumber.isNotBlank()) Text("شاسی: ${inquiry.chassisNumber}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (inquiry.postalCode.isNotBlank() || inquiry.address.isNotBlank()) {
                        Text(
                            text = "آدرس و مشخصات: ${inquiry.address} (کد پستی: ${inquiry.postalCode})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "مبلغ برآوردی تسویه:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%,d تومان".format(inquiry.amount),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "کد پیگیری: ${inquiry.transactionRef.ifBlank { "INQ-" + inquiry.id }}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = dateFormat.format(Date(inquiry.dateMillis)),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!isPaid) {
                        Button(
                            onClick = onApproveByAdmin,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تایید تسویه توسط مدیر", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
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
// DIALOG 1: CHIC INQUIRY FORM WITH COMPLETE VEHICLE & PERSONAL INFO & MESSENGER SELECTION
// =======================================================================================
@Composable
private fun ChicInquiryFormDialog(
    vehicles: List<VehicleEntity>,
    initialType: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (type: String, title: String, plate: String, barcode: String, nationalId: String, amount: Long, name: String, phone: String, vin: String, engine: String, chassis: String, postal: String, addr: String, messenger: String, receiptRef: String) -> Unit
) {
    val inquiryTypes = listOf(
        "استعلام خلافی خودرو",
        "استعلام عوارض آزادراهی",
        "استعلام عوارض سالیانه خودرو",
        "استعلام مالیات نقل و انتقال خودرو"
    )

    val messengers = listOf("روبیکا", "تلگرام", "ایتا", "پیامک / تماس تلفنی")

    var selectedType by remember { mutableStateOf(initialType) }
    var selectedMessenger by remember { mutableStateOf(messengers[0]) }

    var fullName by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var vehiclePlate by remember { mutableStateOf(vehicles.firstOrNull()?.formattedPlate.orEmpty()) }
    var vinCode by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var engineNumber by remember { mutableStateOf("") }
    var chassisNumber by remember { mutableStateOf("") }

    var postalCode by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var receiptRef by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("150000") }

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
                        text = "فرم شیک و جامع استعلام و تسویه خودرو",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "ثبت مشخصات خودرو، اطلاعات فردی و ارسال رسید پرداخت به مدیر",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 1. Inquiry Type Chips (Equal Sized 2x2 Grid)
                item {
                    Text("۱. نوع استعلام:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedType == inquiryTypes[0],
                                onClick = { selectedType = inquiryTypes[0] },
                                label = { Text(inquiryTypes[0], fontSize = 10.5.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            FilterChip(
                                selected = selectedType == inquiryTypes[1],
                                onClick = { selectedType = inquiryTypes[1] },
                                label = { Text(inquiryTypes[1], fontSize = 10.5.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedType == inquiryTypes[2],
                                onClick = { selectedType = inquiryTypes[2] },
                                label = { Text(inquiryTypes[2], fontSize = 10.5.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            FilterChip(
                                selected = selectedType == inquiryTypes[3],
                                onClick = { selectedType = inquiryTypes[3] },
                                label = { Text(inquiryTypes[3], fontSize = 10.5.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // 2. Personal Information
                item {
                    Text("۲. مشخصات فردی متقاضی:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("نام و نام خانوادگی کامل") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nationalId,
                            onValueChange = { if (it.length <= 10) nationalId = it },
                            label = { Text("کد ملی (۱۰ رقم)") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 11) phoneNumber = it },
                            label = { Text("شماره همراه") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                // 3. Vehicle Information
                item {
                    Text("۳. مشخصات خودرو:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = vehiclePlate,
                        onValueChange = { vehiclePlate = it },
                        label = { Text("شماره پلاک ملی خودرو") },
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = vinCode,
                            onValueChange = { if (it.length <= 17) vinCode = it },
                            label = { Text("کد VIN (۱۷ رقم)") },
                            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("بارکد کارت خودرو") },
                            leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
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
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = chassisNumber,
                            onValueChange = { chassisNumber = it },
                            label = { Text("شماره شاسی") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                // 4. Postal Code & Address
                item {
                    Text("۴. کد پستی و آدرس:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { if (it.length <= 10) postalCode = it },
                        label = { Text("کد پستی ۱۰ رقمی محل سکونت") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("آدرس دقیق محل سکونت") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // 5. Preferred Communication Messenger (Equal Sized Grid)
                item {
                    Text("۵. پیام‌رسان ترجیحی ارسال نتیجه:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedMessenger == messengers[0],
                                onClick = { selectedMessenger = messengers[0] },
                                label = { Text(messengers[0], fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            FilterChip(
                                selected = selectedMessenger == messengers[1],
                                onClick = { selectedMessenger = messengers[1] },
                                label = { Text(messengers[1], fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedMessenger == messengers[2],
                                onClick = { selectedMessenger = messengers[2] },
                                label = { Text(messengers[2], fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            FilterChip(
                                selected = selectedMessenger == messengers[3],
                                onClick = { selectedMessenger = messengers[3] },
                                label = { Text(messengers[3], fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // 6. Payment Information to Admin
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "💳 واریز به حساب مدیر: ۶۲۱۹-۸۶۱۹-۲۰۶۹-۶۲۰۹ (میلاد قنواتی)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "پس از واریز مبلغ یا علی‌الحساب، شماره پیگیری را در زیر وارد فرمایید تا مدیر تسویه را انجام دهد.",
                                fontSize = 10.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("مبلغ واریزی (تومان)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = receiptRef,
                            onValueChange = { receiptRef = it },
                            label = { Text("شماره پیگیری فیش") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // Actions
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (fullName.isNotBlank() && vehiclePlate.isNotBlank()) {
                                    onSubmit(
                                        selectedType,
                                        "$selectedType - $vehiclePlate",
                                        vehiclePlate,
                                        barcode,
                                        nationalId.ifBlank { "کد ملی ثبت نشده" },
                                        amount.toLongOrNull() ?: 150000L,
                                        fullName,
                                        phoneNumber,
                                        vinCode,
                                        engineNumber,
                                        chassisNumber,
                                        postalCode,
                                        address,
                                        selectedMessenger,
                                        receiptRef.ifBlank { "واریز به کارت مدیر" }
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
                                Text("ثبت و ارسال به مدیر")
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
