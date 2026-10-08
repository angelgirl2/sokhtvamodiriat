package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import com.angelgirlbrand.modiratsokhtandestelam.ui.components.toPersianDigits
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.ui.draw.shadow
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
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.DatabaseReactiveStatusWidget
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
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val isWideScreen = screenWidth > 600.dp
        
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
        var selectedInquiryForPayment by remember { mutableStateOf<InquiryRecordEntity?>(null) }

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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isWideScreen) 32.dp else 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =====================================================================
            // 1. HERO BANNER
            // =====================================================================
            if (screenWidth > 320.dp) {
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
                                        style = if (isWideScreen) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
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
                                        .size(if (isWideScreen) 56.dp else 46.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(if (isWideScreen) 28.dp else 24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

            item {
                DatabaseReactiveStatusWidget(inquiries = inquiries)
            }

            // =====================================================================
            // 1.5. SPECIALIZED INQUIRY SERVICES GRAPHIC CARDS GRID (کارت‌های گرافیکی مدرن)
            // =====================================================================
            item {
                InquiryServicesGraphicCardsGrid(
                    inquiries = inquiries,
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

                    // 3-Color Dynamic Inquiry Status Tracker (پیگیری هوشمند وضعیت استعلام با زمینه تیره) - Only on large screens
                    if (screenWidth > 400.dp) {
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
                    style = if (screenWidth < 400.dp) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (screenWidth > 350.dp) {
                    TextButton(
                        onClick = { showInquiryDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("استعلام جدید سفارشی", fontSize = 11.sp)
                    }
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
            items(
                items = filteredInquiries,
                key = { it.id }
            ) { inquiry ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    InquiryRecordItem(
                        inquiry = inquiry,
                        onApproveByAdmin = {
                            baleViewModel.approveInquiry(inquiry.id)
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("تایید تسویه توسط مدیر اعمال گردید.")
                        },
                        onDirectPay = {
                            selectedInquiryForPayment = inquiry
                        },
                        onDelete = {},
                        onResendToBot = {
                            baleViewModel.resendInquiryToBot(inquiry)
                        },
                        onViewReceipt = {
                            selectedInquiryForReceipt = inquiry
                        }
                    )
                }
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

    // Modal Dialog: Manager Card Payment & Slip Submission
    if (selectedInquiryForPayment != null) {
        ManagerCardPaymentDialog(
            inquiry = selectedInquiryForPayment!!,
            onDismiss = { selectedInquiryForPayment = null },
            onSubmitSlip = { ref, payer, bank, senderCard, amountPaid, imageUri ->
                baleViewModel.submitPaymentSlip(selectedInquiryForPayment!!, ref, payer, bank, senderCard, amountPaid, imageUri)
                selectedInquiryForPayment = null
            }
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
                    barcodeOrVin = if (barcode.isNotBlank()) barcode else vin,
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
                    barcodeOrVin = if (barcode.isNotBlank()) barcode else vin,
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
                    barcodeOrVin = if (barcode.isNotBlank()) barcode else vin,
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

// Single inquiry record card in history list - Spacious, Clear, Highly Readable, with Status Overlay
@Composable
fun InquiryRecordItem(
    inquiry: InquiryRecordEntity,
    onApproveByAdmin: () -> Unit,
    onDirectPay: () -> Unit,
    onDelete: () -> Unit,
    onResendToBot: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val context = LocalContext.current
    val isApproved = inquiry.isApproved
    val isRejected = inquiry.isRejected

    // Automatic Reactive Color Palette: Approved status uses Color.Green.copy(alpha = 0.2f) and dark green text
    val cardBackgroundColor = when {
        isApproved -> Color.Green.copy(alpha = 0.12f) // Light green background as requested
        isRejected -> Color.Red.copy(alpha = 0.12f)
        else -> Color(0xFFF59E0B).copy(alpha = 0.10f)
    }
    val statusBorderColor = when {
        isApproved -> Color(0xFF047857) // Dark green border
        isRejected -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }
    val statusTextColor = when {
        isApproved -> Color(0xFF064E3B) // Dark green text as requested
        isRejected -> Color(0xFF991B1B)
        else -> Color(0xFF92400E)
    }
    val badgeBgColor = when {
        isApproved -> Color(0xFF047857) // Dark green badge
        isRejected -> Color(0xFFDC2626)
        else -> Color(0xFFD97706)
    }
    // Clean status text without emojis
    val statusLabel = when {
        isApproved -> "تایید شده (Approved)"
        isRejected -> "رد شده(rejected)"
        else -> "در انتظار بررسی توسط مدیر(waiting)"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 4.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp) // Generous card height for clear visibility
                .clickable { onViewReceipt() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.8.dp, statusBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: Status Icon + Title + Plate + Large Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(badgeBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isApproved -> Icons.Default.Check
                                    isRejected -> Icons.Default.Close
                                    else -> Icons.Default.HourglassTop
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = inquiry.inquiryType,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp, // Larger and bolder title
                                color = if (isApproved) statusTextColor else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "پلاک: ${inquiry.plateNumber.ifEmpty { inquiry.barcodeOrVin }}",
                                fontSize = 13.sp, // Larger and clearer plate info
                                fontWeight = FontWeight.Bold,
                                color = if (isApproved) statusTextColor else MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Large, Readable Status Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeBgColor,
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 12.sp, // Bold status label without emojis
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Row 2: Amount & Clear Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (inquiry.amount > 0) {
                        Text(
                            text = "%,d تومان".format(inquiry.amount),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp, // Bold and readable amount
                            color = if (isApproved) statusTextColor else Color(0xFFBE123C)
                        )
                    } else {
                        Text(
                            text = "استعلام رایگان پلاک‌پایه",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isApproved) statusTextColor else Color(0xFF0284C7)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isApproved && inquiry.amount > 0) {
                            Button(
                                onClick = onDirectPay,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("پرداخت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onViewReceipt,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(34.dp),
                            border = BorderStroke(1.2.dp, if (isApproved) Color(0xFF047857) else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        ) {
                            Text(
                                "رسید",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isApproved) statusTextColor else MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = {
                                val shareText = "رسید استعلام و پرداخت\n\n" +
                                        "نوع: ${inquiry.inquiryType}\n" +
                                        "وسیله: ${inquiry.title} (${inquiry.plateNumber})\n" +
                                        "مبلغ: ${String.format("%,d", inquiry.amount)} تومان\n" +
                                        "وضعیت: ${if (isApproved) "تایید شده" else inquiry.status}\n\n" +
                                        "سامانه هوشمند مدیریت خودرو"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری رسید"))
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "اشتراک",
                                tint = if (isApproved) statusTextColor else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onResendToBot,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "ارسال به مدیر",
                                tint = if (isApproved) statusTextColor else Color(0xFF0284C7).copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Corner Status Overlay Layer (تیک سبز برای تایید، ضربدر قرمز برای رد)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = (-7).dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = when {
                isApproved -> Color(0xFF059669) // Emerald Green
                isRejected -> Color(0xFFDC2626) // Vivid Red
                else -> Color(0xFFD97706) // Amber
            },
            border = BorderStroke(1.5.dp, Color.White)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = when {
                        isApproved -> Icons.Default.CheckCircle // Green checkmark for approved
                        isRejected -> Icons.Default.Cancel // Red cross for rejected
                        else -> Icons.Default.HourglassTop
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = when {
                        isApproved -> "تایید شده"
                        isRejected -> "رد شده"
                        else -> "در انتظار بررسی"
                    },
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

// Standalone Inquiry & Request List LazyColumn Component with proper Padding
@Composable
fun InquiryRequestsListLazyColumn(
    inquiries: List<InquiryRecordEntity>,
    onApproveByAdmin: (Long) -> Unit,
    onDirectPay: (InquiryRecordEntity) -> Unit,
    onDelete: (InquiryRecordEntity) -> Unit,
    onResendToBot: (InquiryRecordEntity) -> Unit,
    onViewReceipt: (InquiryRecordEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = inquiries,
            key = { it.id }
        ) { inquiry ->
            InquiryRecordItem(
                inquiry = inquiry,
                onApproveByAdmin = { onApproveByAdmin(inquiry.id) },
                onDirectPay = { onDirectPay(inquiry) },
                onDelete = { onDelete(inquiry) },
                onResendToBot = { onResendToBot(inquiry) },
                onViewReceipt = { onViewReceipt(inquiry) }
            )
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
        subtitle = "کد رهگیری: ${if (inquiry.transactionRef.isNotBlank()) inquiry.transactionRef else "INQ-${inquiry.id}"}",
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
fun ChicInquiryFormDialog(
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



@Composable
fun PaymentTransactionHistorySection(inquiries: List<InquiryRecordEntity>) {
    val context = LocalContext.current
    val paidInquiries = remember(inquiries) {
        inquiries.filter { it.status.contains("پرداخت شد") || it.status.contains("تسویه") }
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
                    Text("تاریخچه تراکنش‌های موفق پرداخت و تسویه", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
                Text(
                    text = if (paidInquiries.isEmpty()) "۰ تراکنش" else "${toPersianDigits(paidInquiries.size.toString())} تراکنش اخیر",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (paidInquiries.isEmpty()) {
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
                            text = "هیچ تراکنشی ثبت نشده است",
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
                paidInquiries.forEach { item ->
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
                                Text(PersianDateHelper.toPersianDateTime(item.dateMillis), fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("%,d تومان".format(item.amount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    "موفق ✅",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
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
    inquiries: List<InquiryRecordEntity> = emptyList(),
    onSelectNegativePoints: () -> Unit,
    onSelectTechnicalInspection: () -> Unit,
    onSelectVehicleDocuments: () -> Unit,
    onSelectHighwayTolls: () -> Unit,
    onSelectFuelCard: () -> Unit,
    onSelectTrafficFines: () -> Unit,
    modifier: Modifier = Modifier
) {
    val negInq = inquiries.firstOrNull { it.inquiryType.contains("نمره منفی") }
    val negStatus = if (negInq?.isApproved == true) "تایید شده" else negInq?.status
    val negDate = negInq?.dateMillis

    val techInq = inquiries.firstOrNull { it.inquiryType.contains("معاینه فنی") }
    val techStatus = if (techInq?.isApproved == true) "تایید شده" else techInq?.status
    val techDate = techInq?.dateMillis

    val docInq = inquiries.firstOrNull { it.inquiryType.contains("اسناد") || it.inquiryType.contains("پلاک") }
    val docStatus = if (docInq?.isApproved == true) "تایید شده" else docInq?.status
    val docDate = docInq?.dateMillis

    val fineInq = inquiries.firstOrNull { it.inquiryType.contains("خلافی") }
    val fineStatus = if (fineInq?.isApproved == true) "تایید شده" else fineInq?.status
    val fineDate = fineInq?.dateMillis

    val tollInq = inquiries.firstOrNull { it.inquiryType.contains("عوارض") || it.inquiryType.contains("آزادراهی") }
    val tollStatus = if (tollInq?.isApproved == true) "تایید شده" else tollInq?.status
    val tollDate = tollInq?.dateMillis

    val fuelInq = inquiries.firstOrNull { it.inquiryType.contains("سوخت") || it.inquiryType.contains("سهمیه") }
    val fuelStatus = if (fuelInq?.isApproved == true) "تایید شده" else fuelInq?.status
    val fuelDate = fuelInq?.dateMillis

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isWideScreen = maxWidth >= 600.dp
        
        Column(
            modifier = Modifier.fillMaxWidth(),
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
                    Icon(
                        Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "استعلام‌های تخصصی و وضعیت زنده",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "آنلاین ⚡",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (isWideScreen) {
                // Wide Screen: 1 Row of 6 Ultra-Compact Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InquiryGraphicCardItem(
                        title = "نمره منفی",
                        subtitle = "سوابق نمره",
                        badge = "راهور",
                        requestStatus = negStatus,
                        icon = Icons.Default.Badge,
                        gradientColors = listOf(Color(0xFF2E1065), Color(0xFF5B21B6)),
                        borderColor = Color(0xFFA78BFA),
                        onClick = onSelectNegativePoints,
                        modifier = Modifier.weight(1f)
                    )
                    InquiryGraphicCardItem(
                        title = "معاینه فنی",
                        subtitle = "گواهی سلامت",
                        badge = "شهرداری",
                        requestStatus = techStatus,
                        icon = Icons.Default.Verified,
                        gradientColors = listOf(Color(0xFF064E3B), Color(0xFF047857)),
                        borderColor = Color(0xFF34D399),
                        onClick = onSelectTechnicalInspection,
                        modifier = Modifier.weight(1f)
                    )
                    InquiryGraphicCardItem(
                        title = "اسناد و پلاک",
                        subtitle = "برگ سبز",
                        badge = "پلیس ۱۰+",
                        requestStatus = docStatus,
                        icon = Icons.Default.FolderSpecial,
                        gradientColors = listOf(Color(0xFF0F172A), Color(0xFF0369A1)),
                        borderColor = Color(0xFF38BDF8),
                        onClick = onSelectVehicleDocuments,
                        modifier = Modifier.weight(1f)
                    )
                    InquiryGraphicCardItem(
                        title = "خلافی راهور",
                        subtitle = "ریز خلافی",
                        badge = "تسویه آنی",
                        requestStatus = fineStatus,
                        icon = Icons.Default.ReceiptLong,
                        gradientColors = listOf(Color(0xFF451A03), Color(0xFF9A3412)),
                        borderColor = Color(0xFFFB923C),
                        onClick = onSelectTrafficFines,
                        modifier = Modifier.weight(1f)
                    )
                    InquiryGraphicCardItem(
                        title = "عوارض آزادراه",
                        subtitle = "بدهی عوارض",
                        badge = "راه‌داری",
                        requestStatus = tollStatus,
                        icon = Icons.Default.Toll,
                        gradientColors = listOf(Color(0xFF1E1B4B), Color(0xFF3730A3)),
                        borderColor = Color(0xFF818CF8),
                        onClick = onSelectHighwayTolls,
                        modifier = Modifier.weight(1f)
                    )
                    InquiryGraphicCardItem(
                        title = "کارت سوخت",
                        subtitle = "سهمیه بنزین",
                        badge = "پالایش",
                        requestStatus = fuelStatus,
                        icon = Icons.Default.LocalGasStation,
                        gradientColors = listOf(Color(0xFF881337), Color(0xFFBE123C)),
                        borderColor = Color(0xFFFB7185),
                        onClick = onSelectFuelCard,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Mobile Handheld: 2 Rows of 3 Ultra-Compact Micro-Cards (High Density)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InquiryGraphicCardItem(
                        title = "نمره منفی",
                        subtitle = "سوابق نمره",
                        badge = "راهور",
                        requestStatus = negStatus,
                        icon = Icons.Default.Badge,
                        gradientColors = listOf(Color(0xFF2E1065), Color(0xFF5B21B6)),
                        borderColor = Color(0xFFA78BFA),
                        onClick = onSelectNegativePoints,
                        modifier = Modifier.weight(1f)
                    )
                    InquiryGraphicCardItem(
                        title = "معاینه فنی",
                        subtitle = "سلامت فنی",
                        badge = "شهرداری",
                        requestStatus = techStatus,
                        icon = Icons.Default.Verified,
                        gradientColors = listOf(Color(0xFF064E3B), Color(0xFF047857)),
                        borderColor = Color(0xFF34D399),
                        onClick = onSelectTechnicalInspection,
                        modifier = Modifier.weight(1f)
                    )
                    InquiryGraphicCardItem(
                        title = "اسناد و پلاک",
                        subtitle = "برگ سبز",
                        badge = "پلیس ۱۰+",
                        requestStatus = docStatus,
                        icon = Icons.Default.FolderSpecial,
                        gradientColors = listOf(Color(0xFF0F172A), Color(0xFF0369A1)),
                        borderColor = Color(0xFF38BDF8),
                        onClick = onSelectVehicleDocuments,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InquiryGraphicCardItem(
                        title = "خلافی راهور",
                        subtitle = "ریز خلافی",
                        badge = "تسویه آنی",
                        requestStatus = fineStatus,
                        icon = Icons.Default.ReceiptLong,
                        gradientColors = listOf(Color(0xFF451A03), Color(0xFF9A3412)),
                        borderColor = Color(0xFFFB923C),
                        onClick = onSelectTrafficFines,
                        modifier = Modifier.weight(1f)
                    )
                    InquiryGraphicCardItem(
                        title = "عوارض آزادراه",
                        subtitle = "بدهی سالانه",
                        badge = "راه‌داری",
                        requestStatus = tollStatus,
                        icon = Icons.Default.Toll,
                        gradientColors = listOf(Color(0xFF1E1B4B), Color(0xFF3730A3)),
                        borderColor = Color(0xFF818CF8),
                        onClick = onSelectHighwayTolls,
                        modifier = Modifier.weight(1f)
                    )
                    InquiryGraphicCardItem(
                        title = "کارت سوخت",
                        subtitle = "سهمیه بنزین",
                        badge = "پالایش",
                        requestStatus = fuelStatus,
                        icon = Icons.Default.LocalGasStation,
                        gradientColors = listOf(Color(0xFF881337), Color(0xFFBE123C)),
                        borderColor = Color(0xFFFB7185),
                        onClick = onSelectFuelCard,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun InquiryGraphicCardItem(
    title: String,
    subtitle: String,
    badge: String,
    requestStatus: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isApproved = requestStatus?.contains("تایید") == true || requestStatus?.contains("پرداخت") == true || requestStatus?.contains("تسویه") == true || requestStatus?.contains("موفق") == true
    val isRejected = requestStatus?.contains("رد") == true

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, if (isApproved) Color(0xFF10B981) else borderColor.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Top Row: Mini Circular Icon + Mini Status Pill / Dot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(19.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    // Mini Status Chip Pill
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when {
                            isApproved -> Color(0xFF10B981)
                            isRejected -> Color(0xFFEF4444)
                            requestStatus != null -> Color(0xFFF59E0B)
                            else -> Color.White.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = when {
                                isApproved -> "تایید ✅"
                                isRejected -> "رد ❌"
                                requestStatus != null -> "بررسی ⏳"
                                else -> badge
                            },
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // Title
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Subtitle
                Text(
                    text = subtitle,
                    fontSize = 7.5.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
                            Text("درخواست استعلام برای مدیر ارسال شد ✅", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("شماره گواهی‌نامه:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(licenseNo, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8), fontSize = 12.5.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("کد ملی متقاضی:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(nationalId, color = Color.White, fontSize = 12.sp)
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "درخواست استعلام نمره منفی با موفقیت برای مدیر ارسال شد و در سوابق استعلام‌های شما ثبت گردید. پاسخ رسمی پس از بررسی توسط مدیر در بخش سوابق اعلام خواهد شد.",
                            fontSize = 11.5.sp,
                            color = Color(0xFFBAE6FD),
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 17.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(44.dp).padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                    ) {
                        Text("مشاهده در سوابق و بستن", fontWeight = FontWeight.Bold)
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
                            Text("درخواست استعلام برای مدیر ارسال شد ✅", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("شماره پلاک:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(plateInput, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8), fontSize = 13.sp)
                    }

                    if (vinInput.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("کد شناسایی (VIN):", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(vinInput, color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "درخواست استعلام معاینه فنی با موفقیت برای مدیر ارسال شد و در سوابق استعلام‌ها ثبت گردید. نتیجه رسمی پس از بررسی توسط مدیر در همین بخش اعلام خواهد شد.",
                            fontSize = 11.5.sp,
                            color = Color(0xFFBAE6FD),
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 17.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(44.dp).padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("مشاهده در سوابق و بستن", fontWeight = FontWeight.Bold)
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
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                            Text("درخواست استعلام اسناد برای مدیر ارسال شد ✅", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    if (vinInput.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("کد شناسایی (VIN):", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(vinInput, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        }
                    }

                    if (barcodeInput.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("بارکد کارت خودرو:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(barcodeInput, color = Color(0xFF38BDF8), fontSize = 12.sp)
                        }
                    }

                    if (nationalIdInput.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("کد ملی متقاضی:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(nationalIdInput, color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "درخواست استعلام اصالت اسناد و وضعیت پلاک با موفقیت برای مدیر ارسال شد و در سوابق ذخیره گردید. پاسخ مدیر در سوابق اعلام خواهد شد.",
                            fontSize = 11.5.sp,
                            color = Color(0xFFBAE6FD),
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 17.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(44.dp).padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("مشاهده در سوابق و بستن", fontWeight = FontWeight.Bold)
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
                            Text("درخواست استعلام ثبت شد", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Text(
                        text = "درخواست استعلام عوارض پلاک «$plateInput» با موفقیت ثبت و برای مدیر ارسال گردید. پاسخ استعلام پس از بررسی در بخش سوابق قابل مشاهده خواهد بود.",
                        fontSize = 11.5.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )

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
                            Text("درخواست استعلام ثبت شد", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Text(
                        text = "درخواست پیگیری کارت سوخت با مشخصات وارد شده با موفقیت برای مدیر ارسال گردید. پس از استعلام از شرکت پالایش و پخش، نتیجه در سوابق به اطلاع شما خواهد رسید.",
                        fontSize = 11.5.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(44.dp).padding(top = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                    ) {
                        Text("بستن و ذخیره در سوابق", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Manager Card Payment & Slip Submission Dialog
@Composable
fun ManagerCardPaymentDialog(
    inquiry: InquiryRecordEntity,
    onDismiss: () -> Unit,
    onSubmitSlip: (receiptRef: String, payerName: String, bankName: String, senderCard: String, amountPaid: Long, imageUri: String) -> Unit
) {
    val context = LocalContext.current
    var payerName by remember { mutableStateOf("") }
    var senderCard by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf(inquiry.amount.toString()) }
    var receiptRef by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("تصویر فیش با موفقیت انتخاب شد")
        }
    }

    val adminCardNumber = "6037-9975-1024-8592"
    val adminCardHolder = "مدیریت سامانه هوشمند استعلام و سوخت"

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "فرم اختصاصی فیش پرداخت و ارسال به مدیر",
        subtitle = "مبلغ استعلام: %,d تومان".format(inquiry.amount),
        icon = Icons.Default.ReceiptLong,
        iconTint = Color(0xFF10B981)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "شماره کارت مدیر جهت واریز وجه:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = adminCardNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Manager Card", adminCardNumber.replace("-", ""))
                                clipboard.setPrimaryClip(clip)
                                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("شماره کارت مدیر کپی شد")
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("کپی کارت", fontSize = 10.sp)
                        }
                    }
                    Text(
                        text = "به نام: $adminCardHolder",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            OutlinedTextField(
                value = payerName,
                onValueChange = { payerName = it },
                label = { Text("نام و نام خانوادگی واریزکننده") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = senderCard,
                onValueChange = { senderCard = it },
                label = { Text("شماره کارت مبدأ (پرداخت‌کننده)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("مبلغ واریزی (تومان)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = receiptRef,
                onValueChange = { receiptRef = it },
                label = { Text("شماره پیگیری فیش بانکی / شماره مرجع (الزامی)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("نام بانک مبدأ (مثلاً ملت، ملی، پاسارگاد)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Gallery Image Picker Button
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedImageUri == null) "انتخاب تصویر فیش از گالری" else "تصویر فیش انتخاب شد (تغییر تصویر)",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (selectedImageUri != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓ پیوست تصویر فیش موفق",
                        fontSize = 11.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { selectedImageUri = null }) {
                        Text("حذف تصویر", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    if (receiptRef.isBlank()) {
                        com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("لطفاً شماره پیگیری فیش بانکی را وارد نمایید")
                    } else {
                        val parsedAmount = amountStr.toLongOrNull() ?: inquiry.amount
                        onSubmitSlip(receiptRef, payerName, bankName, senderCard, parsedAmount, selectedImageUri?.toString() ?: "")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ارسال ساختاریافته فیش به مدیر و ربات بله", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}


