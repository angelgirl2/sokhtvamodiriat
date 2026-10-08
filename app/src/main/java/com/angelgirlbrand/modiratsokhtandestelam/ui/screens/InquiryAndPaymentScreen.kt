package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
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
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.BaleRequestHistoryEntity
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
    val baleMessageId: String = "",
    val isPaid: Boolean = false
)

@Composable
fun InquiryAndPaymentScreen(
    baleViewModel: BaleServiceViewModel,
    fuelViewModel: FuelViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSubmitting by baleViewModel.isSubmitting.collectAsState()
    val vehicles by fuelViewModel.vehicles.collectAsState()
    val baleHistory by baleViewModel.successfulBaleHistory.collectAsState()

    // Real Traffic Fine Inquiry State from ViewModel (Bale Bot Service)
    val activeFineResult by baleViewModel.trafficFineResult.collectAsState()
    val isInquiringFines by baleViewModel.isFineInquiring.collectAsState()

    var showInquiryDialog by remember { mutableStateOf(false) }
    // Specialized Inquiry Dialogs State
    var showNegativePointsDialog by remember { mutableStateOf(false) }
    var showTechnicalInspectionDialog by remember { mutableStateOf(false) }
    var showVehicleDocumentsDialog by remember { mutableStateOf(false) }
    var showHighwayTollsDialog by remember { mutableStateOf(false) }
    var showFuelCardInquiryDialog by remember { mutableStateOf(false) }



    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val compact = maxWidth < 600.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (compact) 10.dp else 16.dp),
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
                onSelectTrafficFines = { com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("فرم استعلام خلافی در همین صفحه در دسترس است.") }
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
        // 3.5. VERIFIED BALE HISTORY ONLY
        // =====================================================================
        item {
            SuccessfulBaleHistorySection(
                history = baleHistory,
                onDelete = { baleViewModel.deleteBaleHistory(it) },
                onClear = { baleViewModel.clearBaleHistory() },
                compact = compact
            )
        }

        // =====================================================================
        // 4. CUSTOM INQUIRY ACTION (no fake local result list)
        // =====================================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ارسال درخواست جدید",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "فقط درخواست‌هایی که واقعاً توسط بله پذیرفته شوند در سابقه بالا ذخیره می‌شوند.",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = { showInquiryDialog = true },
                    enabled = !isSubmitting,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("استعلام جدید", fontSize = 11.5.sp)
                }
            }
        }
    }

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
                    workflowMethod = "BALE_REQUEST",
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
                    amount = 0L,
                    workflowMethod = "EXPERT_REVIEW",
                    fullName = "مالک گواهی‌نامه",
                    phoneNumber = ph
                )
            }
        )
    }

    if (showTechnicalInspectionDialog) {
        TechnicalInspectionInquiryDialog(
            vehicles = vehicles,
            isSubmitting = isSubmitting,
            onDismiss = { showTechnicalInspectionDialog = false },
            onSubmit = { plate, vin ->
                baleViewModel.submitInquiry(
                    inquiryType = "استعلام معاینه فنی خودرو",
                    title = "معاینه فنی",
                    plateNumber = plate,
                    barcodeOrVin = vin,
                    nationalId = "",
                    amount = 0L,
                    workflowMethod = "EXPERT_REVIEW",
                    vinCode = vin
                )
            }
        )
    }

    if (showVehicleDocumentsDialog) {
        VehicleDocumentsInquiryDialog(
            vehicles = vehicles,
            isSubmitting = isSubmitting,
            onDismiss = { showVehicleDocumentsDialog = false },
            onSubmit = { vin, barcode, nat ->
                baleViewModel.submitInquiry(
                    inquiryType = "استعلام اسناد خودرو و وضعیت پلاک",
                    title = "اسناد و برگ سبز",
                    plateNumber = "",
                    barcodeOrVin = barcode.ifBlank { vin },
                    nationalId = nat,
                    amount = 0L,
                    workflowMethod = "EXPERT_REVIEW",
                    vinCode = vin,
                    barcode = barcode
                )
            }
        )
    }

    if (showHighwayTollsDialog) {
        HighwayTollsInquiryDialog(
            vehicles = vehicles,
            isSubmitting = isSubmitting,
            onDismiss = { showHighwayTollsDialog = false },
            onSubmit = { plate ->
                baleViewModel.submitInquiry(
                    inquiryType = "استعلام عوارض آزادراهی و شهرداری",
                    title = "عوارض آزادراهی",
                    plateNumber = plate,
                    barcodeOrVin = "",
                    nationalId = "",
                    amount = 0L,
                    workflowMethod = "DIRECT_PAYMENT"
                )
            }
        )
    }

    if (showFuelCardInquiryDialog) {
        FuelCardInquiryModalDialog(
            vehicles = vehicles,
            isSubmitting = isSubmitting,
            onDismiss = { showFuelCardInquiryDialog = false },
            onSubmit = { vin, barcode, nat ->
                baleViewModel.submitInquiry(
                    inquiryType = "استعلام کارت سوخت هوشمند",
                    title = "کارت سوخت",
                    plateNumber = "",
                    barcodeOrVin = barcode.ifBlank { vin },
                    nationalId = nat,
                    amount = 0L,
                    workflowMethod = "EXPERT_REVIEW",
                    vinCode = vin,
                    barcode = barcode
                )
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

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < 420.dp

        Card(
            modifier = Modifier
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
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "استعلام خلافی فقط با پلاک",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = plateMode == "خودرو",
                            onClick = { plateMode = "خودرو" },
                            label = { Text("خودرو", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = plateMode == "اروندی",
                            onClick = { plateMode = "اروندی" },
                            label = { Text("اروندی 🌴", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = plateMode == "موتور",
                            onClick = { plateMode = "موتور" },
                            label = { Text("موتور", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(selected = plateMode == "خودرو", onClick = { plateMode = "خودرو" }, label = { Text("خودرو", fontSize = 10.5.sp) })
                        FilterChip(selected = plateMode == "اروندی", onClick = { plateMode = "اروندی" }, label = { Text("اروندی 🌴", fontSize = 10.5.sp) })
                        FilterChip(selected = plateMode == "موتور", onClick = { plateMode = "موتور" }, label = { Text("موتور", fontSize = 10.5.sp) })
                    }
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
                    if (compact) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = plateCity,
                                    onValueChange = { if (it.length <= 2) plateCity = it },
                                    label = { Text("ایران") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = plateL3,
                                    onValueChange = { if (it.length <= 3) plateL3 = it },
                                    label = { Text("۳ رقم") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1.1f)) {
                                    IranianPlateLetterPicker(selectedLetter = plateLetter, onLetterSelected = { plateLetter = it })
                                }
                                OutlinedTextField(
                                    value = plateF2,
                                    onValueChange = { if (it.length <= 2) plateF2 = it },
                                    label = { Text("۲ رقم") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(value = plateCity, onValueChange = { if (it.length <= 2) plateCity = it }, label = { Text("ایران") }, modifier = Modifier.weight(0.9f), singleLine = true, shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = plateL3, onValueChange = { if (it.length <= 3) plateL3 = it }, label = { Text("۳ رقم") }, modifier = Modifier.weight(1.1f), singleLine = true, shape = RoundedCornerShape(10.dp))
                            Box(modifier = Modifier.weight(1.1f)) { IranianPlateLetterPicker(selectedLetter = plateLetter, onLetterSelected = { plateLetter = it }) }
                            OutlinedTextField(value = plateF2, onValueChange = { if (it.length <= 2) plateF2 = it }, label = { Text("۲ رقم") }, modifier = Modifier.weight(0.9f), singleLine = true, shape = RoundedCornerShape(10.dp))
                        }
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    onSubmit(
                        selectedType,
                        "وسیله نقلیه",
                        vehiclePlate,
                        "",
                        "",
                        0L,
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
// COMPONENT: SPECIALIZED INQUIRY SERVICES GRAPHIC CARDS GRID (کارت‌های گرافیکی مدرن)
// =============================================================================

@Composable
private fun SuccessfulBaleHistorySection(
    history: List<BaleRequestHistoryEntity>,
    onDelete: (BaleRequestHistoryEntity) -> Unit,
    onClear: () -> Unit,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 12.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text("سوابق ارسال موفق به ربات بله", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        Text(
                            "فقط پیام‌هایی که بله واقعاً پذیرفته است.",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = onClear,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("پاکسازی", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (history.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                            modifier = Modifier.size(28.dp)
                        )
                        Text("هنوز درخواست موفقی برای بله ثبت نشده است.", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            "در صورت خطای شبکه یا رد درخواست توسط بله، چیزی به عنوان استعلام موفق ذخیره نمی‌شود.",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                history.take(20).forEach { record ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(record.requestType, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(record.summary, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(PersianDateHelper.toPersianDateTime(record.dateMillis), fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            IconButton(onClick = { onDelete(record) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

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
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < 430.dp

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Text("خدمات استعلام و ارسال درخواست", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)) {
                    Text("بله ⚡", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            val cards: List<@Composable () -> Unit> = listOf(
                { InquiryGraphicCardItem("نمره منفی راهور", "ارسال درخواست استعلام سوابق", "راهور FARAJA 👮‍♂️", Icons.Default.Badge, listOf(Color(0xFF2E1065), Color(0xFF5B21B6), Color(0xFF7C3AED)), Color(0xFFA78BFA), Color(0xFF4C1D95), Color(0xFFDDD6FE), onSelectNegativePoints) },
                { InquiryGraphicCardItem("معاینه فنی خودرو", "ارسال درخواست اعتبار گواهی", "شهرداری‌ها 🔍", Icons.Default.Verified, listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF059669)), Color(0xFF34D399), Color(0xFF022C22), Color(0xFFA7F3D0), onSelectTechnicalInspection) },
                { InquiryGraphicCardItem("اسناد خودرو و پلاک", "ارسال درخواست وضعیت اسناد", "پلیس ۱۰+ 📄", Icons.Default.FolderSpecial, listOf(Color(0xFF0F172A), Color(0xFF0369A1), Color(0xFF0284C7)), Color(0xFF38BDF8), Color(0xFF0C4A6E), Color(0xFFBAE6FD), onSelectVehicleDocuments) },
                { InquiryGraphicCardItem("خلافی راهور", "ارسال درخواست استعلام پلاک", "کارشناس ⚡", Icons.Default.ReceiptLong, listOf(Color(0xFF451A03), Color(0xFF9A3412), Color(0xFFC2410C)), Color(0xFFFB923C), Color(0xFF7C2D12), Color(0xFFFFEDD5), onSelectTrafficFines) },
                { InquiryGraphicCardItem("عوارض آزادراهی", "ارسال درخواست بدهی عوارض", "راه‌داری 🛣️", Icons.Default.Toll, listOf(Color(0xFF1E1B4B), Color(0xFF3730A3), Color(0xFF4338CA)), Color(0xFF818CF8), Color(0xFF312E81), Color(0xFFE0E7FF), onSelectHighwayTolls) },
                { InquiryGraphicCardItem("کارت سوخت هوشمند", "ارسال درخواست رهگیری کارت", "پالایش و پخش ⛽", Icons.Default.LocalGasStation, listOf(Color(0xFF9F1239), Color(0xFFBE123C), Color(0xFFE11D48)), Color(0xFFFB7185), Color(0xFF881337), Color(0xFFFFE4E6), onSelectFuelCard) }
            )

            if (compact) {
                cards.forEach { it() }
            } else {
                cards.chunked(2).forEach { rowCards ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowCards.forEach {
                            Box(modifier = Modifier.weight(1f)) { it() }
                        }
                        if (rowCards.size == 1) Spacer(modifier = Modifier.weight(1f))
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
    val resultSubmitted = false

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
                        enabled = !isSubmitting,
                        onClick = {
                            onSubmit(licenseNo, nationalId, phone, selectedVehicleTitle)
                            onDismiss()
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
    isSubmitting: Boolean,
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
                        enabled = !isSubmitting,
                        onClick = {
                            onSubmit(plateInput, vinInput)
                            onDismiss()
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
    isSubmitting: Boolean,
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
                        enabled = !isSubmitting,
                        onClick = {
                            onSubmit(vinInput, barcodeInput, nationalIdInput)
                            onDismiss()
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
    isSubmitting: Boolean,
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
                        enabled = !isSubmitting,
                        onClick = {
                            onSubmit(plateInput)
                            onDismiss()
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
    isSubmitting: Boolean,
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
                        enabled = !isSubmitting,
                        onClick = {
                            onSubmit(vinInput, barcodeInput, nationalIdInput)
                            onDismiss()
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


