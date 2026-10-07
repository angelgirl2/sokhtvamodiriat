package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceReminderEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.M3ThemedDialogContainer
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.VehicleFormDialog
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.VehiclePlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.InsuranceAgencyPickerField
import com.angelgirlbrand.modiratsokhtandestelam.util.InsuranceAgencies
import com.angelgirlbrand.modiratsokhtandestelam.util.PersianDateHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: Long,
    fuelViewModel: FuelViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehicles by fuelViewModel.vehicles.collectAsState()
    val allReminders by fuelViewModel.reminders.collectAsState()
    val allFuelLogs by fuelViewModel.fuelLogs.collectAsState()

    val vehicle = vehicles.firstOrNull { it.id == vehicleId }

    var showEditVehicleDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showEditInsuranceDialog by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    if (vehicle == null) {
        // Vehicle not found or just deleted
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    val vehicleReminders = remember(allReminders, vehicle.id) {
        allReminders.filter { it.vehicleId == vehicle.id }
    }
    val vehicleLogs = remember(allFuelLogs, vehicle.id) {
        allFuelLogs.filter { it.vehicleId == vehicle.id }
    }

    // Days remaining for insurance
    val currentTime = System.currentTimeMillis()
    val insuranceDaysRemaining = remember(vehicle.effectiveInsuranceExpiryMillis, currentTime) {
        ((vehicle.effectiveInsuranceExpiryMillis - currentTime) / (24 * 3600 * 1000L)).toInt()
    }

    // 1. Edit Vehicle Dialog
    if (showEditVehicleDialog) {
        VehicleFormDialog(
            vehicleToEdit = vehicle,
            onDismiss = { showEditVehicleDialog = false },
            onConfirm = { title, f2, letter, l3, city, fuelType, cap, odo, vType ->
                val updated = vehicle.copy(
                    title = title,
                    plateFirst2 = f2,
                    plateLetter = letter,
                    plateLast3 = l3,
                    plateCityCode = city,
                    fuelType = fuelType,
                    tankCapacity = cap,
                    currentOdometer = odo,
                    vehicleType = vType
                )
                fuelViewModel.updateVehicle(updated)
                showEditVehicleDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("اطلاعات با موفقیت بروزرسانی شد")
            }
        )
    }

    // 2. Delete Confirmation Dialog (Custom Themed Dialog)
    if (showDeleteConfirmDialog) {
        com.angelgirlbrand.modiratsokhtandestelam.ui.components.CustomThemedConfirmDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = "حذف وسیله نقلیه از برنامه",
            message = "آیا از حذف کامل «${vehicle.title}» (${vehicle.formattedPlate}) و تمام سوابق سوخت و یادآورهای آن اطمینان دارید؟",
            confirmText = "حذف قطعی",
            dismissText = "انصراف",
            isDestructive = true,
            icon = Icons.Default.DeleteOutline,
            onConfirm = {
                fuelViewModel.deleteVehicle(vehicle)
                showDeleteConfirmDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وسیله نقلیه با موفقیت حذف شد")
                onNavigateBack()
            }
        )
    }

    // 3. Add Reminder Dialog (Oil, Brakes, Belts, etc.)
    if (showAddReminderDialog) {
        AddVehicleReminderDialog(
            vehicle = vehicle,
            onDismiss = { showAddReminderDialog = false },
            onConfirm = { serviceType, targetDate, targetOdo, notes ->
                fuelViewModel.addServiceReminder(
                    vehicleId = vehicle.id,
                    serviceType = serviceType,
                    targetDateMillis = targetDate,
                    targetOdometer = targetOdo,
                    notes = notes
                )
                showAddReminderDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("یادآور سرویس $serviceType با موفقیت ثبت شد")
            }
        )
    }

    // 4. Edit Insurance Date Dialog
    if (showEditInsuranceDialog) {
        EditInsuranceDialog(
            vehicle = vehicle,
            onDismiss = { showEditInsuranceDialog = false },
            onConfirm = { expiryMillis, company, type ->
                fuelViewModel.updateVehicleInsurance(vehicle, expiryMillis, company, type)
                showEditInsuranceDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("اطلاعات بیمه‌نامه ذخیره شد")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "جزئیات ${if (vehicle.isMotorcycle) "موتورسیکلت" else "خودرو"}: ${vehicle.title}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditVehicleDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // One-time interactive educational guide card for Vehicle Details
            item {
                val secManager = remember {
                    (context.applicationContext as com.angelgirlbrand.modiratsokhtandestelam.FuelApplication).securityManager
                }
                com.angelgirlbrand.modiratsokhtandestelam.ui.components.SectionIntroGuideCard(
                    sectionKey = "vehicle_detail_overview",
                    title = "راهنمای کارت و جزئیات وسیله نقلیه",
                    description = "مشاهده مشخصات کامل پلاک، سهمیه سوخت، پایش لحظه‌ای باک و تاریخچه سوخت‌گیری‌های این خودرو.",
                    tips = listOf(
                        "با لمس دکمه ویرایش در بالا می‌توانید ظرفیت باک و مشخصات پلاک را تغییر دهید.",
                        "سوابق مصرف و سرویس‌های ویژه این خودرو در پایین صفحه دسته‌بندی شده‌اند."
                    ),
                    icon = Icons.Default.DirectionsCar,
                    accentColor = Color(0xFF0284C7),
                    securityManager = secManager
                )
            }

            // -----------------------------------------------------------------
            // 1. AUTHENTIC LICENSE PLATE BANNER
            // -----------------------------------------------------------------
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    vehicle.isMotorcycle -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                                    vehicle.isArvand -> Color(0xFF0284C7).copy(alpha = 0.15f)
                                    vehicle.plateLetter == "ت" || vehicle.plateLetter == "ع" -> Color(0xFFFACC15).copy(alpha = 0.25f)
                                    vehicle.plateLetter == "♿" -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                }
                            ) {
                                Text(
                                    text = when {
                                        vehicle.isMotorcycle -> "🏍️ موتورسیکلت"
                                        vehicle.isArvand -> "🌴 منطقه آزاد اروند"
                                        vehicle.plateLetter == "ت" -> "🚕 تاکسی"
                                        vehicle.plateLetter == "ع" -> "🚛 ناوگان عمومی"
                                        vehicle.plateLetter == "♿" -> "♿ معلولین و جانبازان"
                                        else -> "⛽ ${vehicle.fuelType}"
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        vehicle.isMotorcycle -> Color(0xFF7C3AED)
                                        vehicle.isArvand -> Color(0xFF0284C7)
                                        vehicle.plateLetter == "ت" || vehicle.plateLetter == "ع" -> Color(0xFFB45309)
                                        vehicle.plateLetter == "♿" -> Color(0xFF1D4ED8)
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "شناسه وسیله: #${vehicle.id}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Complete Iranian License Plate in Standard Size
                        VehiclePlateView(
                            vehicle = vehicle
                        )
                    }
                }
            }

            // -----------------------------------------------------------------
            // 2. VEHICLE SPECIFICATIONS
            // -----------------------------------------------------------------
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "مشخصات فنی وسیله نقلیه",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SpecItem(label = "مدل وسیله", value = vehicle.title)
                            SpecItem(label = "کیلومتر کارکرد", value = "${String.format("%,d", vehicle.currentOdometer)} km")
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SpecItem(label = "نوع سوخت", value = vehicle.fuelType)
                            SpecItem(label = "ظرفیت باک", value = "${vehicle.tankCapacity.toInt()} لیتر")
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // 3. INSURANCE REMINDER CARD (یادآور بیمه خودرو/موتور)
            // -----------------------------------------------------------------
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (insuranceDaysRemaining <= 15) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = CardDefaults.outlinedCardBorder()
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (insuranceDaysRemaining <= 15) MaterialTheme.colorScheme.error else Color(0xFF1D4ED8)
                                )
                                Text(
                                    text = "وضعیت بیمه‌نامه (${vehicle.insuranceType})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            FilledTonalButton(
                                onClick = { showEditInsuranceDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("تنظیم تاریخ", fontSize = 11.sp)
                            }
                        }

                        val dateStr = PersianDateHelper.toPersianDate(vehicle.effectiveInsuranceExpiryMillis)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "شرکت بیمه‌گر: ${vehicle.insuranceCompany}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "تاریخ انقضا: $dateStr",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (insuranceDaysRemaining <= 0) MaterialTheme.colorScheme.error
                                else if (insuranceDaysRemaining <= 30) Color(0xFFD97706)
                                else Color(0xFF10B981)
                            ) {
                                Text(
                                    text = if (insuranceDaysRemaining <= 0) "منقضی شده!"
                                    else "$insuranceDaysRemaining روز باقی‌مانده",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // 4. PERIODIC MAINTENANCE REMINDERS (روغن موتور، لنت، فیلتر)
            // -----------------------------------------------------------------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "یادآورهای سرویس و نگهداری (${vehicleReminders.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                    TextButton(
                        onClick = { showAddReminderDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تنظیم سرویس جدید", fontSize = 12.sp)
                    }
                }
            }

            if (vehicleReminders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Text("یادآوری برای تعویض روغن یا سرویس ثبت نشده است.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(
                                onClick = { showAddReminderDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text("تنظیم تعویض روغن و سرویس دوره‌ای", fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            } else {
                items(vehicleReminders) { reminder ->
                    VehicleReminderItem(
                        reminder = reminder,
                        currentOdometer = vehicle.currentOdometer,
                        onToggleComplete = { fuelViewModel.toggleReminderCompleted(reminder) },
                        onDelete = { fuelViewModel.deleteReminder(reminder) }
                    )
                }
            }

            // -----------------------------------------------------------------
            // 5. QUICK ACTIONS IN DETAIL SCREEN (ویرایش، حذف، تنظیم سرویس)
            // -----------------------------------------------------------------
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showEditVehicleDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ویرایش اطلاعات", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حذف وسیله نقلیه", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun VehicleReminderItem(
    reminder: ServiceReminderEntity,
    currentOdometer: Int,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val kmRemaining = reminder.targetOdometer - currentOdometer
    val dateStr = PersianDateHelper.toPersianDate(reminder.targetDateMillis)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = reminder.isCompleted,
                    onCheckedChange = { onToggleComplete() }
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = reminder.serviceType,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "موعد کیلومتر: ${String.format("%,d", reminder.targetOdometer)} km (سررسید: $dateStr)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (kmRemaining > 0 && !reminder.isCompleted) {
                        Text(
                            text = "باقی‌مانده: ${String.format("%,d", kmRemaining)} کیلومتر تا تعویض",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (kmRemaining <= 500) Color(0xFFDC2626) else Color(0xFF059669)
                        )
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Dialog: Add Service Reminder for Vehicle (روغن موتور، لنت، فیلتر)
@Composable
fun AddVehicleReminderDialog(
    vehicle: VehicleEntity,
    onDismiss: () -> Unit,
    onConfirm: (serviceType: String, targetDate: Long, targetOdo: Int, notes: String) -> Unit
) {
    val commonServices = if (vehicle.isMotorcycle) {
        listOf("تعویض روغن موتور و فیلتر", "تنظیم و روغن‌کاری زنجیر", "لنت ترمز عقب/جلو", "تعویض شمع موتور", "سرویس دوره‌ای آچارکشی")
    } else {
        listOf("تعویض روغن موتور و فیلتر", "تعویض لنت ترمز", "تعویض فیلتر هوا و اتاق", "تعویض تسمه تایم", "تعویض روغن گیربکس", "سرویس دوره‌ای کامل")
    }

    var selectedService by remember { mutableStateOf(commonServices[0]) }
    var targetOdometerStr by remember {
        mutableStateOf((vehicle.currentOdometer + (if (vehicle.isMotorcycle) 2000 else 5000)).toString())
    }
    var notes by remember { mutableStateOf("") }
    var monthsAhead by remember { mutableStateOf(3) }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "تنظیم یادآور سرویس و مصرفی",
        subtitle = "خودرو ${vehicle.title}",
        icon = Icons.Default.NotificationsActive,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Text("نوع سرویس یا قطعه:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    commonServices.take(4).forEach { svc ->
                        FilterChip(
                            selected = selectedService == svc,
                            onClick = {
                                selectedService = svc
                                // Default mileage intervals
                                if (svc.contains("روغن")) {
                                    targetOdometerStr = (vehicle.currentOdometer + (if (vehicle.isMotorcycle) 2000 else 5000)).toString()
                                } else if (svc.contains("لنت")) {
                                    targetOdometerStr = (vehicle.currentOdometer + 25000).toString()
                                } else if (svc.contains("تسمه")) {
                                    targetOdometerStr = (vehicle.currentOdometer + 60000).toString()
                                }
                            },
                            label = { Text(svc, fontSize = 11.5.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                OutlinedTextField(
                    value = targetOdometerStr,
                    onValueChange = { targetOdometerStr = it },
                    label = { Text("کیلومتر سررسید (کیلومتر فعلی: ${String.format("%,d", vehicle.currentOdometer)})") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text("موعد زمانی پیشنهادی:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1 to "۱ ماه بعد", 3 to "۳ ماه بعد", 6 to "۶ ماه بعد", 12 to "۱ سال بعد").forEach { (m, label) ->
                        FilterChip(
                            selected = monthsAhead == m,
                            onClick = { monthsAhead = m },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("یادداشت / برند روغن یا قطعه (اختیاری)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val odo = targetOdometerStr.toIntOrNull() ?: (vehicle.currentOdometer + 5000)
                            val targetDate = System.currentTimeMillis() + (monthsAhead * 30L * 24 * 3600 * 1000L)
                            onConfirm(selectedService, targetDate, odo, notes)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ثبت یادآور")
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

// Dialog: Edit Insurance Date for Vehicle
@Composable
fun EditInsuranceDialog(
    vehicle: VehicleEntity,
    onDismiss: () -> Unit,
    onConfirm: (expiryMillis: Long, company: String, type: String) -> Unit
) {
    val companies = listOf("بیمه ایران", "بیمه آسیا", "بیمه دانا", "بیمه البرز", "بیمه پاسارگاد", "بیمه پارسیان")
    var selectedCompany by remember { mutableStateOf(vehicle.insuranceCompany.ifEmpty { "بیمه ایران" }) }
    var selectedType by remember { mutableStateOf(vehicle.insuranceType.ifEmpty { "بیمه شخص ثالث" }) }
    var monthsDuration by remember { mutableStateOf(12) }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "تنظیم بیمه‌نامه خودرو",
        subtitle = "وسیله نقلیه ${vehicle.title}",
        icon = Icons.Default.Security,
        iconTint = Color(0xFF0284C7)
    ) {
        Text("نوع بیمه:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("بیمه شخص ثالث", "بیمه بدنه", "بیمه موتورسیکلت").forEach { t ->
                FilterChip(
                    selected = selectedType == t,
                    onClick = { selectedType = t },
                    label = { Text(t, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        InsuranceAgencyPickerField(
            selectedAgency = selectedCompany,
            onAgencySelected = { selectedCompany = it },
            label = "نمایندگی / شرکت بیمه‌گر:",
            modifier = Modifier.fillMaxWidth()
        )

        Text("اعتبار از امروز:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(3 to "۳ ماهه", 6 to "۶ ماهه", 12 to "۱ ساله").forEach { (m, label) ->
                FilterChip(
                    selected = monthsDuration == m,
                    onClick = { monthsDuration = m },
                    label = { Text(label, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val newExpiry = System.currentTimeMillis() + (monthsDuration * 30L * 24 * 3600 * 1000L)
                    onConfirm(newExpiry, selectedCompany, selectedType)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ذخیره تاریخ")
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
