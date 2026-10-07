package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.InquiryRecordEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.ArvandPlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.DynamicStatusWaitingTracker
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.IranianMotorcyclePlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.IranianPlateLetterPicker
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.IranianPlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.M3ThemedDialogContainer
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.SmartAdaptivePlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.VehiclePlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel
import com.angelgirlbrand.modiratsokhtandestelam.util.PersianDateHelper
import kotlinx.coroutines.launch

// Model for individual traffic violations
data class FineViolationItem(
    val code: String,
    val title: String,
    val location: String,
    val dateTimeShamsi: String,
    val amount: Long,
    val billId: String,
    val paymentId: String,
    val cameraRecorded: Boolean
)

// Model for complete traffic fine inquiry result
data class TrafficFineResult(
    val vehicleTitle: String,
    val plateFormatted: String,
    val isMotorcycle: Boolean,
    val totalAmount: Long,
    val violationCount: Int,
    val negativePoints: Int,
    val judicialStatus: String,
    val inquiryDateTimeShamsi: String,
    val violations: List<FineViolationItem>,
    val inquiryRecordId: Long = 0L,
    val isPaid: Boolean = false
)

@Composable
fun InquiryAndPaymentScreen(
    baleViewModel: BaleServiceViewModel,
    fuelViewModel: FuelViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val inquiries by baleViewModel.inquiries.collectAsState()
    val isSubmitting by baleViewModel.isSubmitting.collectAsState()
    val vehicles by fuelViewModel.vehicles.collectAsState()

    // Real Traffic Fine Inquiry State from ViewModel (Bale Bot Service)
    val activeFineResult by baleViewModel.trafficFineResult.collectAsState()
    val isInquiringFines by baleViewModel.isFineInquiring.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("همه استعلام‌ها", "خلافی راهور", "عوارض آزادراهی", "عوارض سالیانه", "مالیات نقل و انتقال")

    var showInquiryDialog by remember { mutableStateOf(false) }
    var selectedInquiryForReceipt by remember { mutableStateOf<InquiryRecordEntity?>(null) }

    // Specialized Inquiry Dialogs State
    var showNegativePointsDialog by remember { mutableStateOf(false) }
    var showTechnicalInspectionDialog by remember { mutableStateOf(false) }
    var showVehicleDocumentsDialog by remember { mutableStateOf(false) }
    var showHighwayTollsDialog by remember { mutableStateOf(false) }
    var showFuelCardInquiryDialog by remember { mutableStateOf(false) }

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
        // =====================================================================
        // 1. HERO BANNER
        // =====================================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inquiry_hero_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                        text = "سامانه استعلام خلافی و خدمات کارشناس و مدیر",
                                        color = Color(0xFFE0F2FE),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "استعلام خلافی فقط با پلاک و ارسال به کارشناس و مدیر",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "ثبت شماره پلاک خودرو یا موتورسیکلت و ارسال مستقیم درخواست استعلام به کارشناس و مدیر",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFBAE6FD)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 1.5. SPECIALIZED INQUIRY SERVICES GRAPHIC CARDS GRID (کارت‌های گرافیکی مدرن)
        // =====================================================================
        item {
            InquiryServicesGraphicCardsGrid(
                onSelectNegativePoints = { showNegativePointsDialog = true },
                onSelectTechnicalInspection = { showTechnicalInspectionDialog = true },
                onSelectVehicleDocuments = { showVehicleDocumentsDialog = true },
                onSelectHighwayTolls = { showHighwayTollsDialog = true },
                onSelectFuelCard = { showFuelCardInquiryDialog = true },
                onSelectTrafficFines = { selectedTabIndex = 1 }
            )
        }

        // =====================================================================
        // 2. DEDICATED TRAFFIC FINE RESULTS CARD (کارت اختصاصی ثبت و ارسال به بله)
        // =====================================================================
        if (activeFineResult != null) {
            item {
                TrafficFineDedicatedResultCard(
                    result = activeFineResult!!,
                    onDismiss = { baleViewModel.clearTrafficFineResult() },
                    onOpenBaleChat = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ble.ir"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("پیام‌رسان بله باز شد")
                        }
                    },
                    onShareReport = {
                        val reportText = buildString {
                            appendLine("📋 رسید ارسال استعلام خلافی به کارشناس و مدیر")
                            appendLine("═══════════════════")
                            appendLine("🚗 وسیله: ${activeFineResult!!.vehicleTitle} (${activeFineResult!!.plateFormatted})")
                            appendLine("📅 زمان ارسال: ${activeFineResult!!.inquiryDateTimeShamsi}")
                            appendLine("🆔 وضعیت: ${activeFineResult!!.judicialStatus}")
                            appendLine("═══════════════════")
                            appendLine("ثبت شده در اپلیکیشن مدیریت خودرو و سوخت")
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری رسید استعلام"))
                    }
                )
            }
        }

        // =====================================================================
        // 3. PLATE INQUIRY FORM CARD (استعلام با شماره پلاک خودرو یا موتور)
        // =====================================================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isInquiringFines) {
                    DynamicStatusWaitingTracker(
                        isProcessing = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TrafficFinePlateInquiryCard(
                    vehicles = vehicles,
                    isLoading = isInquiringFines,
                    onInquire = { isMotorcycle, title, pF2, pLetter, pL3, pCity, motoTop3, motoBottom5, plateFormatted ->
                        baleViewModel.queryTrafficFinesByPlate(
                            isMotorcycle = isMotorcycle,
                            vehicleTitle = title,
                            plateF2 = pF2,
                            plateLetter = pLetter,
                            plateL3 = pL3,
                            plateCity = pCity,
                            motoTop3 = motoTop3,
                            motoBottom5 = motoBottom5,
                            plateFormatted = plateFormatted,
                            onComplete = {
                                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("درخواست برای ادمین ارسال شد")
                            }
                        )
                    }
                )

                // 3-Color Dynamic Inquiry Status Tracker (پیگیری هوشمند وضعیت استعلام با زمینه تیره)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                    Icons.Default.Timeline,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "پیگیری وضعیت سه‌رنگ استعلام آنلاین:",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.5.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "راهور و پایانه ⚡",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // The 3 Cycling Colors (🟠 نارنجی -> 🔵 آبی -> 🟢 سبز) - Dark Theme Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Stage 1: Warm Orange (نارنجی)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFFFB923C).copy(alpha = 0.8f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFB923C)))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("۱. صف استعلام", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDBA74), textAlign = TextAlign.Center)
                                    Text("بررسی پلاک", fontSize = 8.5.sp, color = Color(0xFFFED7AA), textAlign = TextAlign.Center)
                                }
                            }

                            // Stage 2: Sky Blue (آبی)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.8f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF38BDF8)))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("۲. پردازش سرور", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7DD3FC), textAlign = TextAlign.Center)
                                    Text("اتصال راهور", fontSize = 8.5.sp, color = Color(0xFFBAE6FD), textAlign = TextAlign.Center)
                                }
                            }

                            // Stage 3: Emerald Green (سبز)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.8f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF34D399)))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("۳. صدور خلافی", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6EE7B7), textAlign = TextAlign.Center)
                                    Text("تسویه آنی", fontSize = 8.5.sp, color = Color(0xFFA7F3D0), textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 3.5. PAYMENT TRANSACTION HISTORY SECTION (تاریخچه تراکنش‌های عوارض و خلافی)
        // =====================================================================
        item {
            PaymentTransactionHistorySection()
        }

        // =====================================================================
        // 4. TAB SELECTOR FOR PAST INQUIRIES
        // =====================================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سوابق استعلام‌ها و درخواست‌ها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { showInquiryDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("استعلام جدید سفارشی", fontSize = 11.5.sp)
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
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

        // =====================================================================
        // 5. INQUIRIES HISTORY LIST
        // =====================================================================
        if (filteredInquiries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "هنوز سابقه استعلامی در این بخش ثبت نشده است.",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "با استفاده از کادر بالای صفحه، شماره پلاک خودرو یا موتور خود را به کارشناس و مدیر ارسال نمایید.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredInquiries) { inquiry ->
                InquiryRecordItem(
                    inquiry = inquiry,
                    onApproveByAdmin = {
                        baleViewModel.approveInquiry(inquiry.id)
                        com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("تایید تسویه توسط مدیر اعمال گردید.")
                    },
                    onDirectPay = {
                        baleViewModel.processDirectPayment(inquiry)
                        TransactionHistoryManager.addTransaction(
                            context,
                            PaymentTransactionRecord(
                                title = "پرداخت ${inquiry.title} (${inquiry.plateNumber})",
                                amount = "${String.format("%,d", inquiry.amount)} تومان",
                                status = "موفق ✅",
                                date = "امروز • درگاه پرداخت امن شاپرک"
                            )
                        )
                    },
                    onDelete = {
                        baleViewModel.deleteInquiry(inquiry)
                        com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("استعلام از سوابق حذف گردید")
                    },
                    onViewReceipt = {
                        selectedInquiryForReceipt = inquiry
                    }
                )
            }
        }
    }

    // Modal Dialog: Detailed Payment Receipt
    if (selectedInquiryForReceipt != null) {
        ReceiptDetailsDialog(
            inquiry = selectedInquiryForReceipt!!,
            onDismiss = { selectedInquiryForReceipt = null }
        )
    }

    // Modal Dialog: Custom Inquiry Form
    if (showInquiryDialog) {
        ChicInquiryFormDialog(
            vehicles = vehicles,
            initialType = "استعلام خلافی خودرو",
            isSubmitting = isSubmitting,
            onDismiss = { showInquiryDialog = false },
            onSubmit = { type, title, plate, barcode, nationalId, amount, name, phone, vin, engine, chassis, postal, addr, messenger, ref ->
                baleViewModel.submitInquiry(
                    inquiryType = type,
                    title = title,
                    plateNumber = plate,
                    barcodeOrVin = barcode.ifBlank { vin },
                    nationalId = nationalId,
                    amount = amount,
                    workflowMethod = if (ref.isNotBlank()) "DIRECT_PAYMENT" else "EXPERT_REVIEW",
                    fullName = name,
                    phoneNumber = phone,
                    vinCode = vin,
                    barcode = barcode,
                    engineNumber = engine,
                    chassisNumber = chassis,
                    postalCode = postal,
                    address = addr
                )
                showInquiryDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("استعلام جدید با موفقیت برای ادمین ارسال شد")
            }
        )
    }

    // Specialized Inquiry Dialogs
    if (showNegativePointsDialog) {
        NegativePointsInquiryDialog(
            vehicles = vehicles,
            isSubmitting = isSubmitting,
            onDismiss = { showNegativePointsDialog = false },
            onSubmit = { lic, nat, ph, vTitle ->
                baleViewModel.submitInquiry(
                    inquiryType = "استعلام نمره منفی گواهی‌نامه",
                    title = "گواهی‌نامه $vTitle",
                    plateNumber = lic,
                    barcodeOrVin = lic,
                    nationalId = nat,
                    amount = 12000L,
                    workflowMethod = "EXPERT_REVIEW",
                    fullName = "مالک گواهی‌نامه",
                    phoneNumber = ph
                )
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("درخواست استعلام نمره منفی ثبت گردید")
            }
        )
    }

    if (showTechnicalInspectionDialog) {
        TechnicalInspectionInquiryDialog(
            vehicles = vehicles,
            onDismiss = { showTechnicalInspectionDialog = false },
            onSubmit = { plate, vin ->
                baleViewModel.submitInquiry(
                    inquiryType = "استعلام معاینه فنی خودرو",
                    title = "معاینه فنی",
                    plateNumber = plate,
                    barcodeOrVin = vin,
                    nationalId = "",
                    amount = 15000L,
                    workflowMethod = "EXPERT_REVIEW",
                    vinCode = vin
                )
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("درخواست استعلام معاینه فنی ثبت گردید")
            }
        )
    }

    if (showVehicleDocumentsDialog) {
        VehicleDocumentsInquiryDialog(
            vehicles = vehicles,
            onDismiss = { showVehicleDocumentsDialog = false },
            onSubmit = { vin, barcode, nat ->
                baleViewModel.submitInquiry(
                    inquiryType = "استعلام اسناد خودرو و وضعیت پلاک",
                    title = "اسناد و برگ سبز",
                    plateNumber = "",
                    barcodeOrVin = barcode.ifBlank { vin },
                    nationalId = nat,
                    amount = 20000L,
                    workflowMethod = "EXPERT_REVIEW",
                    vinCode = vin,
                    barcode = barcode
                )
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("درخواست استعلام اسناد خودرو ثبت گردید")
            }
        )
    }

    if (showHighwayTollsDialog) {
        HighwayTollsInquiryDialog(
            vehicles = vehicles,
            onDismiss = { showHighwayTollsDialog = false },
            onSubmit = { plate ->
                baleViewModel.submitInquiry(
                    inquiryType = "استعلام عوارض آزادراهی و شهرداری",
                    title = "عوارض آزادراهی",
                    plateNumber = plate,
                    barcodeOrVin = "",
                    nationalId = "",
                    amount = 35000L,
                    workflowMethod = "DIRECT_PAYMENT"
                )
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("درخواست استعلام عوارض ثبت گردید")
            }
        )
    }

    if (showFuelCardInquiryDialog) {
        FuelCardInquiryModalDialog(
            vehicles = vehicles,
            onDismiss = { showFuelCardInquiryDialog = false },
            onSubmit = { vin, barcode, nat ->
                baleViewModel.submitInquiry(
                    inquiryType = "استعلام کارت سوخت هوشمند",
                    title = "کارت سوخت",
                    plateNumber = "",
                    barcodeOrVin = barcode.ifBlank { vin },
                    nationalId = nat,
                    amount = 10000L,
                    workflowMethod = "EXPERT_REVIEW",
                    vinCode = vin,
                    barcode = barcode
                )
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("درخواست استعلام کارت سوخت ثبت گردید")
            }
        )
    }
}

// =============================================================================
// COMPONENT: DEDICATED TRAFFIC FINE RESULTS CARD (کارت اختصاصی ثبت و ارسال به بله)
// =============================================================================

@Composable
fun TrafficFineDedicatedResultCard(
    result: TrafficFineResult,
    onDismiss: () -> Unit,
    onOpenBaleChat: () -> Unit,
    onShareReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("traffic_fine_result_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Status badge & Dismiss Button
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
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "درخواست استعلام خلافی به کارشناس و مدیر ارسال شد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                        Text(
                            text = "زمان ارسال: ${result.inquiryDateTimeShamsi}",
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // License plate banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "شماره پلاک استعلام‌شده: ${result.plateFormatted}",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF38BDF8)
                )
            }

            // Key Metrics Summary Badges (وضعیت ارسال، نوع وسیله، پلاک)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Status
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF064E3B),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "وضعیت درخواست",
                            fontSize = 10.sp,
                            color = Color(0xFFA7F3D0)
                        )
                        Text(
                            text = "ارسال به کارشناس و مدیر ✅",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp,
                            color = Color.White
                        )
                    }
                }

                // Metric 2: Vehicle Type
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "نوع وسیله",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = if (result.isMotorcycle) "موتورسیکلت" else "خودرو سواری",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = Color.White
                        )
                    }
                }

                // Metric 3: Mode
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "نحوه استعلام",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "فقط با پلاک",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            // Explanation notice box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0369A1).copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "برای ادمین ارسال شد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF7DD3FC)
                        )
                        Text(
                            text = "درخواست استعلام پلاک «${result.plateFormatted}» با موفقیت برای ادمین ارسال شد.",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            // Bottom Action Buttons: Open Bale Chat & Share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenBaleChat,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاهده در پیام‌رسان بله", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onShareReport,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اشتراک‌گذاری", fontSize = 11.5.sp)
                }
            }
        }
    }
}

// =============================================================================
// COMPONENT: PLATE INQUIRY FORM CARD (استعلام خلافی با پلاک خودرو یا موتور)
// =============================================================================

@Composable
fun TrafficFinePlateInquiryCard(
    vehicles: List<VehicleEntity>,
    isLoading: Boolean,
    onInquire: (
        isMotorcycle: Boolean,
        vehicleTitle: String,
        plateF2: String,
        plateLetter: String,
        plateL3: String,
        plateCity: String,
        motoTop3: String,
        motoBottom5: String,
        plateFormatted: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    // Plate mode: "خودرو" (ملی), "اروندی" (منطقه آزاد), "موتور" (موتورسیکلت)
    var plateMode by remember {
        mutableStateOf(
            when {
                vehicles.firstOrNull()?.isMotorcycle == true -> "موتور"
                vehicles.firstOrNull()?.isArvand == true -> "اروندی"
                else -> "خودرو"
            }
        )
    }

    // Car plate fields
    var plateF2 by remember { mutableStateOf(vehicles.firstOrNull { !it.isMotorcycle && !it.isArvand }?.plateFirst2 ?: "12") }
    var plateLetter by remember { mutableStateOf(vehicles.firstOrNull { !it.isMotorcycle && !it.isArvand }?.plateLetter ?: "ب") }
    var plateL3 by remember { mutableStateOf(vehicles.firstOrNull { !it.isMotorcycle && !it.isArvand }?.plateLast3 ?: "345") }
    var plateCity by remember { mutableStateOf(vehicles.firstOrNull { !it.isMotorcycle && !it.isArvand }?.plateCityCode ?: "11") }

    // Arvand plate field (5 digits)
    var arvandDigits by remember {
        mutableStateOf(
            vehicles.firstOrNull { it.isArvand }?.let { it.plateLast3.ifEmpty { it.plateFirst2 } } ?: "12365"
        )
    }

    // Motorcycle plate fields
    var motoTop3 by remember { mutableStateOf(vehicles.firstOrNull { it.isMotorcycle }?.plateFirst2 ?: "123") }
    var motoBottom5 by remember { mutableStateOf(vehicles.firstOrNull { it.isMotorcycle }?.plateLast3 ?: "45678") }

    var vehicleTitle by remember { mutableStateOf(vehicles.firstOrNull()?.title ?: "پژو ۲۰۶") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("traffic_fine_inquiry_input_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "استعلام خلافی فقط با پلاک",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )

                // Vehicle Type Segmented Chips: خودرو ملی، پلاک اروندی، موتورسیکلت
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = plateMode == "خودرو",
                        onClick = { plateMode = "خودرو" },
                        label = { Text("خودرو", fontSize = 10.5.sp) }
                    )
                    FilterChip(
                        selected = plateMode == "اروندی",
                        onClick = { plateMode = "اروندی" },
                        label = { Text("اروندی 🌴", fontSize = 10.5.sp) }
                    )
                    FilterChip(
                        selected = plateMode == "موتور",
                        onClick = { plateMode = "موتور" },
                        label = { Text("موتور", fontSize = 10.5.sp) }
                    )
                }
            }

            // Quick select from user's registered vehicles in Room
            if (vehicles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "انتخاب از وسایل نقلیه من:",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(vehicles) { v ->
                            SuggestionChip(
                                onClick = {
                                    vehicleTitle = v.title
                                    when {
                                        v.isMotorcycle -> {
                                            plateMode = "موتور"
                                            motoTop3 = v.plateFirst2
                                            motoBottom5 = v.plateLast3
                                        }
                                        v.isArvand -> {
                                            plateMode = "اروندی"
                                            arvandDigits = v.plateLast3.ifEmpty { v.plateFirst2 }.ifEmpty { "12365" }
                                        }
                                        else -> {
                                            plateMode = "خودرو"
                                            plateF2 = v.plateFirst2
                                            plateLetter = v.plateLetter
                                            plateL3 = v.plateLast3
                                            plateCity = v.plateCityCode
                                        }
                                    }
                                },
                                label = { Text("${v.title} (${v.formattedPlate})", fontSize = 11.sp, color = Color.White) },
                                icon = {
                                    Icon(
                                        when {
                                            v.isMotorcycle -> Icons.Default.TwoWheeler
                                            else -> Icons.Default.DirectionsCar
                                        },
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Graphical Authentic License Plate Live Preview with Automatic Color Changes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                SmartAdaptivePlateView(
                    first2 = plateF2,
                    letter = if (plateMode == "اروندی") "اروند" else if (plateMode == "موتور") "موتور" else plateLetter,
                    last3 = plateL3,
                    cityCode = plateCity,
                    isArvand = plateMode == "اروندی",
                    arvandNumber = arvandDigits,
                    isMotorcycle = plateMode == "موتور",
                    motoTop3 = motoTop3,
                    motoBottom5 = motoBottom5,
                    isLarge = true
                )
            }

            // Plate Input Fields
            when (plateMode) {
                "اروندی" -> {
                    // Arvand 5-digit Input Field
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = arvandDigits,
                            onValueChange = { if (it.length <= 5) arvandDigits = it },
                            label = { Text("شماره ۵ رقمی پلاک اروند (مثلاً ۱۲۳۶۵)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            supportingText = {
                                Text("پلاک منطقه آزاد اروند شامل ۵ رقم فارسی در بالا و ۵ رقم انگلیسی در پایین است", color = Color(0xFF94A3B8))
                            }
                        )
                    }
                }
                "موتور" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = motoTop3,
                            onValueChange = { if (it.length <= 3) motoTop3 = it },
                            label = { Text("۳ رقم بالا") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = motoBottom5,
                            onValueChange = { if (it.length <= 5) motoBottom5 = it },
                            label = { Text("۵ رقم پایین") },
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
                        // Persian Letter Picker with Live Color Swatch Indicator
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

            // National ID field for inquiry
            var nationalCode by remember { mutableStateOf("") }
            OutlinedTextField(
                value = nationalCode,
                onValueChange = { if (it.length <= 10) nationalCode = it },
                label = { Text("کد ملی مالک خودرو (الزامی برای استعلام راهور)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color(0xFF38BDF8),
                    unfocusedLabelColor = Color(0xFF94A3B8)
                )
            )

            // Inquiry Button
            Button(
                onClick = {
                    val isMoto = plateMode == "موتور"
                    val formatted = when (plateMode) {
                        "اروندی" -> "اروند $arvandDigits"
                        "موتور" -> "$motoTop3 - $motoBottom5"
                        else -> "$plateF2 $plateLetter $plateL3 ایران $plateCity"
                    }
                    onInquire(isMoto, vehicleTitle, if (plateMode == "اروندی") arvandDigits else plateF2, if (plateMode == "اروندی") "اروند" else plateLetter, plateL3, plateCity, motoTop3, motoBottom5, formatted)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("در حال ارسال درخواست به کارشناس و مدیر...", fontSize = 12.sp, color = Color.White)
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (plateMode) {
                            "موتور" -> "ارسال درخواست استعلام خلافی موتورسیکلت"
                            "اروندی" -> "ارسال درخواست استعلام خلافی پلاک اروندی"
                            else -> "ارسال درخواست استعلام خلافی خودرو"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Single inquiry record card in history list
@Composable
fun InquiryRecordItem(
    inquiry: InquiryRecordEntity,
    onApproveByAdmin: () -> Unit,
    onDirectPay: () -> Unit,
    onDelete: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val isPaid = inquiry.status.contains("پرداخت شد") || inquiry.status.contains("تسویه")
    val isBaleSent = inquiry.status.contains("ارسال به مدیریت") || inquiry.status.contains("ربات بله") || inquiry.workflowMethod == "BALE_BOT"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewReceipt() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        color = if (isPaid || isBaleSent) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF43F5E).copy(alpha = 0.15f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isPaid || isBaleSent) Icons.Default.CheckCircle else Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = if (isPaid || isBaleSent) Color(0xFF10B981) else Color(0xFFF43F5E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(text = inquiry.inquiryType, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = inquiry.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPaid) Color(0xFF10B981).copy(alpha = 0.15f) else if (isBaleSent) Color(0xFF0284C7).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isPaid) "تسویه شد" else if (isBaleSent) "ارسال به مدیریت" else "در انتظار پرداخت",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) Color(0xFF047857) else if (isBaleSent) Color(0xFF0369A1) else Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "پلاک: ${inquiry.plateNumber}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (inquiry.amount > 0) {
                    Text(
                        text = "%,d تومان".format(inquiry.amount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isPaid) Color(0xFF047857) else Color(0xFFBE123C)
                    )
                } else {
                    Text(
                        text = "استعلام پلاک‌پایه",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = Color(0xFF0284C7)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = PersianDateHelper.toPersianDateTime(inquiry.dateMillis),
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!isPaid && inquiry.amount > 0) {
                        Button(
                            onClick = onDirectPay,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("پرداخت آنی", fontSize = 10.5.sp)
                        }
                    }
                }
            }
        }
    }
}

// Receipt details modal dialog
@Composable
fun ReceiptDetailsDialog(
    inquiry: InquiryRecordEntity,
    onDismiss: () -> Unit
) {
    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "رسید استعلام و پرداخت",
        subtitle = "کد رهگیری: ${inquiry.transactionRef.ifBlank { "INQ-${inquiry.id}" }}",
        icon = Icons.Default.ReceiptLong,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("نوع استعلام: ${inquiry.inquiryType}", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            Text("وسیله نقلیه: ${inquiry.title}", fontSize = 12.sp)
            Text("شماره پلاک: ${inquiry.plateNumber}", fontSize = 12.sp)
            if (inquiry.amount > 0) {
                Text("مبلغ: %,d تومان".format(inquiry.amount), fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Text("وضعیت: ${inquiry.status}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("تاریخ و زمان: ${PersianDateHelper.toPersianDateTime(inquiry.dateMillis)}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("بستن رسید", fontWeight = FontWeight.Bold)
        }
    }
}

// Dialog for custom general inquiry
@Composable
private fun ChicInquiryFormDialog(
    vehicles: List<VehicleEntity>,
    initialType: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (type: String, title: String, plate: String, barcode: String, nationalId: String, amount: Long, name: String, phone: String, vin: String, engine: String, chassis: String, postal: String, addr: String, messenger: String, receiptRef: String) -> Unit
) {
    val inquiryTypes = listOf(
        "استعلام خلافی خودرو و موتورسیکلت",
        "استعلام نمره منفی گواهی‌نامه",
        "استعلام وضعیت گواهی‌نامه و سوابق",
        "استعلام معاینه فنی خودرو",
        "استعلام اسناد خودرو و وضعیت پلاک",
        "استعلام عوارض آزادراهی",
        "استعلام عوارض سالیانه شهرداری",
        "استعلام مالیات نقل و انتقال خودرو"
    )
    var selectedType by remember { mutableStateOf(initialType) }
    var vehiclePlate by remember { mutableStateOf(vehicles.firstOrNull()?.formattedPlate.orEmpty()) }
    var amount by remember { mutableStateOf("150000") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "ثبت استعلام جدید",
        subtitle = "انتخاب خدمات و استعلام آنلاین",
        icon = Icons.Default.Search,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        OutlinedTextField(
            value = vehiclePlate,
            onValueChange = { vehiclePlate = it },
            label = { Text("شماره پلاک") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("مبلغ برآوردی (تومان)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    onSubmit(
                        selectedType,
                        "وسیله نقلیه",
                        vehiclePlate,
                        "",
                        "",
                        amount.toLongOrNull() ?: 150000L,
                        fullName,
                        phoneNumber,
                        "", "", "", "", "", "پیامک", ""
                    )
                },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ثبت استعلام", fontWeight = FontWeight.Bold)
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

// =============================================================================
// 3.5. PAYMENT TRANSACTION HISTORY SECTION (تاریخچه تراکنش‌های عوارض و خلافی - شروع از صفر)
// =============================================================================

data class PaymentTransactionRecord(
    val title: String,
    val amount: String,
    val status: String,
    val date: String
)

object TransactionHistoryManager {
    private const val PREFS_NAME = "payment_transactions_history_v2"
    private const val KEY_TRANSACTIONS = "transactions_json"

    fun getTransactions(context: Context): List<PaymentTransactionRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        return try {
            val jsonArray = org.json.JSONArray(jsonStr)
            val list = mutableListOf<PaymentTransactionRecord>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    PaymentTransactionRecord(
                        title = obj.getString("title"),
                        amount = obj.getString("amount"),
                        status = obj.getString("status"),
                        date = obj.optString("date", "امروز • درگاه پرداخت امن شاپرک")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addTransaction(context: Context, record: PaymentTransactionRecord) {
        val current = getTransactions(context).toMutableList()
        current.add(0, record)
        saveTransactions(context, current)
    }

    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_TRANSACTIONS).apply()
    }

    private fun saveTransactions(context: Context, list: List<PaymentTransactionRecord>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = org.json.JSONArray()
        list.forEach { item ->
            val obj = org.json.JSONObject().apply {
                put("title", item.title)
                put("amount", item.amount)
                put("status", item.status)
                put("date", item.date)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_TRANSACTIONS, jsonArray.toString()).apply()
    }
}

private fun toPersianDigits(input: String): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return input.map { ch ->
        if (ch in '0'..'9') persianDigits[ch - '0'] else ch
    }.joinToString("")
}

@Composable
private fun PaymentTransactionHistorySection() {
    val context = LocalContext.current
    var transactions by remember {
        mutableStateOf(TransactionHistoryManager.getTransactions(context))
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text("تاریخچه تراکنش‌های پرداخت عوارض و خلافی", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
                Text(
                    text = if (transactions.isEmpty()) "۰ تراکنش" else "${toPersianDigits(transactions.size.toString())} تراکنش اخیر",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (transactions.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "هیچ تراکنشی ثبت نشده است (۰ تراکنش)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "پس از استعلام و پرداخت موفق خلافی یا عوارض، رسید و سوابق تراکنش‌ها در اینجا نمایش داده خواهند شد.",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                transactions.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                Text(item.date, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(item.amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    item.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.status.contains("موفق")) Color(0xFF16A34A) else Color(0xFFD97706)
                                )
                            }
                        }
                    }
                }

                // Clear history option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            TransactionHistoryManager.clearAll(context)
                            transactions = emptyList()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("پاکسازی سوابق", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// =============================================================================
// COMPONENT: SPECIALIZED INQUIRY SERVICES GRAPHIC CARDS GRID (کارت‌های گرافیکی مدرن)
// =============================================================================

@Composable
fun InquiryServicesGraphicCardsGrid(
    onSelectNegativePoints: () -> Unit,
    onSelectTechnicalInspection: () -> Unit,
    onSelectVehicleDocuments: () -> Unit,
    onSelectHighwayTolls: () -> Unit,
    onSelectFuelCard: () -> Unit,
    onSelectTrafficFines: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                    Icons.Default.Dashboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "لیست استعلام‌های تخصصی راهور و اسناد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "آنلاین ⚡",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // 1. Row: Rahvar Negative Points & Technical Inspection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Rahvar Negative Points (نمره منفی راهور)
            InquiryGraphicCardItem(
                title = "نمره منفی راهور",
                subtitle = "استعلام گواهی‌نامه و سوابق نمره منفی",
                badge = "راهور FARAJA 👮‍♂️",
                icon = Icons.Default.Badge,
                gradientColors = listOf(Color(0xFF2E1065), Color(0xFF5B21B6), Color(0xFF7C3AED)),
                borderColor = Color(0xFFA78BFA),
                badgeBg = Color(0xFF4C1D95),
                badgeText = Color(0xFFDDD6FE),
                onClick = onSelectNegativePoints,
                modifier = Modifier.weight(1f)
            )

            // Card 2: Technical Inspection (معاینه فنی)
            InquiryGraphicCardItem(
                title = "معاینه فنی خودرو",
                subtitle = "اعتبار گواهی سلامت و استعلام تاریخ",
                badge = "شهرداری‌ها 🔍",
                icon = Icons.Default.Verified,
                gradientColors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF059669)),
                borderColor = Color(0xFF34D399),
                badgeBg = Color(0xFF022C22),
                badgeText = Color(0xFFA7F3D0),
                onClick = onSelectTechnicalInspection,
                modifier = Modifier.weight(1f)
            )
        }

        // 2. Row: Vehicle Documents & Traffic Fines
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 3: Vehicle Documents & Title Deed (اسناد خودرو)
            InquiryGraphicCardItem(
                title = "اسناد خودرو و پلاک",
                subtitle = "کارت خودرو، برگ سبز و سوابق مالکیت",
                badge = "پلیس ۱۰+ 📄",
                icon = Icons.Default.FolderSpecial,
                gradientColors = listOf(Color(0xFF0F172A), Color(0xFF0369A1), Color(0xFF0284C7)),
                borderColor = Color(0xFF38BDF8),
                badgeBg = Color(0xFF0C4A6E),
                badgeText = Color(0xFFBAE6FD),
                onClick = onSelectVehicleDocuments,
                modifier = Modifier.weight(1f)
            )

            // Card 4: Traffic Violations & Fines (خلافی راهور)
            InquiryGraphicCardItem(
                title = "خلافی راهور و تسویه",
                subtitle = "ریز خلافی، اعتراض و تسویه آنی",
                badge = "تسویه آنی ⚡",
                icon = Icons.Default.ReceiptLong,
                gradientColors = listOf(Color(0xFF451A03), Color(0xFF9A3412), Color(0xFFC2410C)),
                borderColor = Color(0xFFFB923C),
                badgeBg = Color(0xFF7C2D12),
                badgeText = Color(0xFFFFEDD5),
                onClick = onSelectTrafficFines,
                modifier = Modifier.weight(1f)
            )
        }

        // 3. Row: Highway Tolls & Fuel Card Inquiry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 5: Highway Tolls & Municipal Taxes (عوارض آزادراهی)
            InquiryGraphicCardItem(
                title = "عوارض آزادراهی",
                subtitle = "بدهی آزادراه‌ها، بدهی طرح و سالیانه",
                badge = "راه‌داری 🛣️",
                icon = Icons.Default.Toll,
                gradientColors = listOf(Color(0xFF1E1B4B), Color(0xFF3730A3), Color(0xFF4338CA)),
                borderColor = Color(0xFF818CF8),
                badgeBg = Color(0xFF312E81),
                badgeText = Color(0xFFE0E7FF),
                onClick = onSelectHighwayTolls,
                modifier = Modifier.weight(1f)
            )

            // Card 6: Fuel Card Tracking (پیگیری کارت سوخت)
            InquiryGraphicCardItem(
                title = "کارت سوخت هوشمند",
                subtitle = "رهگیری پستی و تخصیص سهمیه سوخت",
                badge = "پالایش و پخش ⛽",
                icon = Icons.Default.LocalGasStation,
                gradientColors = listOf(Color(0xFF881337), Color(0xFFBE123C), Color(0xFFE11D48)),
                borderColor = Color(0xFFFB7185),
                badgeBg = Color(0xFF4C0519),
                badgeText = Color(0xFFFFE4E6),
                onClick = onSelectFuelCard,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InquiryGraphicCardItem(
    title: String,
    subtitle: String,
    badge: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    borderColor: Color,
    badgeBg: Color,
    badgeText: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.2.dp, borderColor.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradientColors))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Badge & Top Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeBg.copy(alpha = 0.85f),
                        border = BorderStroke(0.8.dp, borderColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeText,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Title
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.5.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Subtitle
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )

                // Action Footer Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "استعلام فوری",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// SPECIALIZED DIALOG 1: RAHVAR NEGATIVE POINTS INQUIRY (استعلام نمره منفی راهور)
// =============================================================================

@Composable
fun NegativePointsInquiryDialog(
    vehicles: List<VehicleEntity>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (licenseNo: String, nationalId: String, phone: String, vehicleTitle: String) -> Unit
) {
    var licenseNo by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedVehicleTitle by remember { mutableStateOf(vehicles.firstOrNull()?.title ?: "خودرو شخص") }
    var resultSubmitted by remember { mutableStateOf(false) }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "استعلام نمره منفی راهور و گواهی‌نامه",
        subtitle = "بررسی نمره منفی گواهی‌نامه و سوابق تخلفات رانندگی",
        icon = Icons.Default.Badge,
        iconTint = Color(0xFFA78BFA)
    ) {
        if (!resultSubmitted) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = licenseNo,
                    onValueChange = { if (it.length <= 10) licenseNo = it },
                    label = { Text("شماره ۱۰ رقمی گواهی‌نامه") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = nationalId,
                    onValueChange = { if (it.length <= 10) nationalId = it },
                    label = { Text("کد ملی دارنده گواهی‌نامه") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 11) phone = it },
                    label = { Text("شماره تلفن همراه (جهت دریافت پیامک)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSubmit(licenseNo, nationalId, phone, selectedVehicleTitle)
                            resultSubmitted = true
                        },
                        modifier = Modifier.weight(1.2f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9))
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استعلام فوری نمره منفی", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.8f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.2.dp, Color(0xFF10B981))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(22.dp))
                            Text("وضعیت گواهی‌نامه: معتبر و فعال ✅", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("نمره منفی ثبت شده:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("۰ امتیاز (سوابق کاملاً پاک)", fontWeight = FontWeight.ExtraBold, color = Color(0xFF34D399), fontSize = 12.5.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سقف مجاز نمره منفی:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("۳۰ امتیاز (مرحله اول)", color = Color.White, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("وضعیت ضبط گواهی‌نامه:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("بدون محرومیت و ضبط", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(44.dp).padding(top = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                    ) {
                        Text("تایید و بستن", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =============================================================================
// SPECIALIZED DIALOG 2: TECHNICAL INSPECTION INQUIRY (استعلام معاینه فنی)
// =============================================================================

@Composable
fun TechnicalInspectionInquiryDialog(
    vehicles: List<VehicleEntity>,
    onDismiss: () -> Unit,
    onSubmit: (plate: String, vin: String) -> Unit
) {
    var selectedVehicle by remember { mutableStateOf(vehicles.firstOrNull()) }
    var plateInput by remember { mutableStateOf(selectedVehicle?.formattedPlate ?: "") }
    var vinInput by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "استعلام معاینه فنی خودرو",
        subtitle = "پیگیری گواهی سلامت فنی، تاریخ انقضا و مرکز معاینه",
        icon = Icons.Default.Verified,
        iconTint = Color(0xFF34D399)
    ) {
        if (!showResult) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (vehicles.isNotEmpty()) {
                    Text("انتخاب خودرو:", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(vehicles) { v ->
                            FilterChip(
                                selected = selectedVehicle?.id == v.id,
                                onClick = {
                                    selectedVehicle = v
                                    plateInput = v.formattedPlate
                                },
                                label = { Text(v.title, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = plateInput,
                    onValueChange = { plateInput = it },
                    label = { Text("شماره پلاک خودرو") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = vinInput,
                    onValueChange = { if (it.length <= 17) vinInput = it.uppercase() },
                    label = { Text("کد VIN / شماره شاسی (۱۷ رقمی)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSubmit(plateInput, vinInput)
                            showResult = true
                        },
                        modifier = Modifier.weight(1.2f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استعلام معاینه فنی", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.8f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.2.dp, Color(0xFF10B981))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(22.dp))
                            Text("گواهی معاینه فنی معتبر است ✅", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("اعتبار تا تاریخ:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("۱۴۰۴/۰۹/۲۰", fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8), fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مرکز صادرکننده:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("مرکز معاینه فنی مکانیزه شماره ۱", color = Color.White, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("وضعیت تست آلایندگی:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("تایید (معاینه فنی برتر)", color = Color(0xFF34D399), fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(44.dp).padding(top = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("تایید و ثبت در یادآورها", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =============================================================================
// SPECIALIZED DIALOG 3: VEHICLE DOCUMENTS & TITLE DEED (اسناد خودرو و پلاک)
// =============================================================================

@Composable
fun VehicleDocumentsInquiryDialog(
    vehicles: List<VehicleEntity>,
    onDismiss: () -> Unit,
    onSubmit: (vin: String, barcode: String, nationalId: String) -> Unit
) {
    var vinInput by remember { mutableStateOf("") }
    var barcodeInput by remember { mutableStateOf("") }
    var nationalIdInput by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "استعلام اسناد خودرو و وضعیت پلاک",
        subtitle = "بررسی کارت خودرو، برگ سبز، اسناد مالکیت و رهگیری پستی",
        icon = Icons.Default.FolderSpecial,
        iconTint = Color(0xFF38BDF8)
    ) {
        if (!showResult) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = vinInput,
                    onValueChange = { if (it.length <= 17) vinInput = it.uppercase() },
                    label = { Text("کد ۱۷ رقمی VIN خودرو") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = { if (it.length <= 9) barcodeInput = it },
                    label = { Text("شماره بارکد پستی کارت خودرو (۸ یا ۹ رقم)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = nationalIdInput,
                    onValueChange = { if (it.length <= 10) nationalIdInput = it },
                    label = { Text("کد ملی مالک سند") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSubmit(vinInput, barcodeInput, nationalIdInput)
                            showResult = true
                        },
                        modifier = Modifier.weight(1.2f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استعلام اسناد و پلاک", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.8f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.2.dp, Color(0xFF38BDF8))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                            Text("اصالت اسناد مالکیت تایید شد ✅", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("وضعیت برگ سبز (سند):", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("صادر و تحویل مالک گردیده", fontWeight = FontWeight.Bold, color = Color(0xFF34D399), fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("کارت هوشمند خودرو:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("تحویل پست / کد رهگیری صادر شد", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("وضعیت فک پلاک:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("فعال به نام مالک جدید", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(44.dp).padding(top = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("بستن و ذخیره در سوابق", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =============================================================================
// SPECIALIZED DIALOG 4: HIGHWAY TOLLS (عوارض آزادراهی)
// =============================================================================

@Composable
fun HighwayTollsInquiryDialog(
    vehicles: List<VehicleEntity>,
    onDismiss: () -> Unit,
    onSubmit: (plate: String) -> Unit
) {
    var plateInput by remember { mutableStateOf(vehicles.firstOrNull()?.formattedPlate ?: "") }
    var showResult by remember { mutableStateOf(false) }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "استعلام عوارض آزادراهی و شهرداری",
        subtitle = "بررسی و تسویه آنی عوارض آزادراه‌های کشور و شهرداری",
        icon = Icons.Default.Toll,
        iconTint = Color(0xFF818CF8)
    ) {
        if (!showResult) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = plateInput,
                    onValueChange = { plateInput = it },
                    label = { Text("شماره پلاک خودرو یا موتور") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSubmit(plateInput)
                            showResult = true
                        },
                        modifier = Modifier.weight(1.2f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA))
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استعلام عوارض", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.8f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.2.dp, Color(0xFF818CF8))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(22.dp))
                            Text("وضعیت بدهی عوارض آزادراهی", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مبلغ بدهی آزادراه‌ها:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("۰ تومان (فاقد بدهی) ✅", fontWeight = FontWeight.ExtraBold, color = Color(0xFF34D399), fontSize = 12.5.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("وضعیت عوارض سالیانه:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("تسویه تا پایان سال جاری", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(44.dp).padding(top = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA))
                    ) {
                        Text("تایید و بستن", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =============================================================================
// SPECIALIZED DIALOG 5: FUEL CARD INQUIRY (استعلام کارت سوخت)
// =============================================================================

@Composable
fun FuelCardInquiryModalDialog(
    vehicles: List<VehicleEntity>,
    onDismiss: () -> Unit,
    onSubmit: (vin: String, barcode: String, nationalId: String) -> Unit
) {
    var vinInput by remember { mutableStateOf("") }
    var barcodeInput by remember { mutableStateOf("") }
    var nationalIdInput by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "پیگیری و استعلام کارت سوخت",
        subtitle = "استعلام صدور، رهگیری مرسوله پستی و سهمیه بنزین",
        icon = Icons.Default.LocalGasStation,
        iconTint = Color(0xFFFB7185)
    ) {
        if (!showResult) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = vinInput,
                    onValueChange = { if (it.length <= 17) vinInput = it.uppercase() },
                    label = { Text("کد ۱۷ رقمی VIN خودرو") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = { if (it.length <= 16) barcodeInput = it },
                    label = { Text("کد رهگیری پستی یا شماره بارکد") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = nationalIdInput,
                    onValueChange = { if (it.length <= 10) nationalIdInput = it },
                    label = { Text("کد ملی مالک کارت سوخت") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSubmit(vinInput, barcodeInput, nationalIdInput)
                            showResult = true
                        },
                        modifier = Modifier.weight(1.2f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استعلام کارت سوخت", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.8f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.2.dp, Color(0xFFFB7185))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Color(0xFFFB7185), modifier = Modifier.size(22.dp))
                            Text("وضعیت کارت سوخت هوشمند", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("وضعیت صدور کارت:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("صادر شده و فعال ✅", fontWeight = FontWeight.Bold, color = Color(0xFF34D399), fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سهمیه ماهانه:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("۶۰ لیتر (۱۵,۰۰۰ ریال) + ۱۰۰ لیتر آزاد", color = Color(0xFF38BDF8), fontSize = 11.5.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ارسال پستی:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("تحویل مقصد گردیده است", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(44.dp).padding(top = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                    ) {
                        Text("بستن و ذخیره", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


