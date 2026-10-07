package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Inventory
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
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.FuelConsumptionChart
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.FuelAnalysisWidget
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.M3ThemedDialogContainer
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.SmartAdaptivePlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.VehicleFormDialog
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.VehicleOverviewCard
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.LiveRequestStatusWidget
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.SmartFuelCard
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.CameraQrScannerDialog
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModel
import com.angelgirlbrand.modiratsokhtandestelam.util.PersianDateHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FuelManagementScreen(
    fuelViewModel: FuelViewModel,
    baleViewModel: BaleServiceViewModel,
    onRequestInsuranceInBale: ((VehicleEntity) -> Unit)? = null,
    onNavigateToReminders: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehicles by fuelViewModel.vehicles.collectAsState()
    val fuelLogs by fuelViewModel.fuelLogs.collectAsState()
    val reminders by fuelViewModel.reminders.collectAsState()
    val totalCost by fuelViewModel.totalCost.collectAsState()
    val totalLiters by fuelViewModel.totalLiters.collectAsState()
    val selectedVehicleId by fuelViewModel.selectedVehicleId.collectAsState()

    val serviceRequests by baleViewModel.serviceRequests.collectAsState()
    val inquiries by baleViewModel.inquiries.collectAsState()

    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<VehicleEntity?>(null) }
    var showAddLogDialog by remember { mutableStateOf(false) }
    var showQuickAddReminderDialog by remember { mutableStateOf(false) }
    var vehicleToDelete by remember { mutableStateOf<VehicleEntity?>(null) }
    var logToDelete by remember { mutableStateOf<FuelLogEntity?>(null) }

    val activeVehicle = remember(vehicles, selectedVehicleId) {
        vehicles.firstOrNull { it.id == selectedVehicleId } ?: vehicles.firstOrNull()
    }

    val filteredLogs = remember(fuelLogs, activeVehicle) {
        if (activeVehicle == null) fuelLogs
        else fuelLogs.filter { it.vehicleId == activeVehicle.id }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header & Title ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مدیریت خودرو و سوخت",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "ثبت، پایش هوشمند و تحلیل هزینه‌های بنزین",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddVehicleDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("خودرو جدید")
                }
            }
        }

        // --- Empty State if no vehicles (strictly starting from zero) ---
        if (vehicles.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "هنوز هیچ خودرویی اضافه نشده است",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "برنامه با صفر داده شروع می‌شود. برای شروع مدیریت سوخت و سرویس‌ها، اولین خودرو را ثبت کنید.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddVehicleDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("ثبت اولین خودرو")
                        }
                    }
                }
            }
        } else {
            // --- Vehicle Switcher Carousel ---
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(vehicles) { v ->
                        val isSelected = activeVehicle?.id == v.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { fuelViewModel.selectVehicle(v.id) },
                            label = { Text(v.title) },
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
            }

            // --- 1.1 Live Status Widget at Top of Dashboard ---
            item {
                LiveRequestStatusWidget(
                    serviceRequests = serviceRequests,
                    inquiries = inquiries,
                    onNavigateToServices = {
                        if (onRequestInsuranceInBale != null) {
                            onRequestInsuranceInBale(activeVehicle ?: vehicles.first())
                        }
                    },
                    onNavigateToInquiries = {
                        if (onRequestInsuranceInBale != null) {
                            onRequestInsuranceInBale(activeVehicle ?: vehicles.first())
                        }
                    }
                )
            }

            // --- Active Vehicle Overview Component (Fuel, Insurance, Service Reminders) ---
            if (activeVehicle != null) {
                item {
                    VehicleOverviewCard(
                        vehicle = activeVehicle,
                        recentFuelLogs = fuelLogs,
                        reminders = reminders,
                        onAddFuelClick = { showAddLogDialog = true },
                        onAddReminderClick = { showQuickAddReminderDialog = true },
                        onRequestInsuranceClick = {
                            if (onRequestInsuranceInBale != null) {
                                onRequestInsuranceInBale(activeVehicle)
                            } else {
                                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("درخواست تمدید بیمه در بخش «درخواست خدمات» آماده ثبت است")
                            }
                        },
                        onToggleReminderComplete = { reminder ->
                            fuelViewModel.toggleReminderCompleted(reminder)
                        },
                        onUpdateInsurance = { expiryMillis, company, type ->
                            fuelViewModel.updateVehicleInsurance(activeVehicle, expiryMillis, company, type)
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("اطلاعات بیمه‌نامه ذخیره شد")
                        },
                        onDeleteVehicleClick = { vehicleToDelete = activeVehicle },
                        onEditVehicleClick = { vehicleToEdit = activeVehicle },
                        onNavigateToReminders = onNavigateToReminders
                    )
                }

                // --- 1.2 Interactive 3D Flipping Fuel Card ---
                item {
                    SmartFuelCard(vehicle = activeVehicle)
                }
            }

            // --- Stats Summary Cards ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "کل هزینه سوخت",
                        value = "%,d تومان".format(totalCost),
                        icon = Icons.Default.Payments,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "مجموع بنزین",
                        value = "%.1f لیتر".format(totalLiters),
                        icon = Icons.Default.LocalGasStation,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- Smart Driving Analysis Widget (AI Insights) ---
            if (activeVehicle != null) {
                item {
                    FuelAnalysisWidget(logs = filteredLogs)
                }
            }

            // --- Interactive Monthly Fuel Consumption Chart ---
            item {
                FuelConsumptionChart(logs = filteredLogs)
            }

            // --- Action Buttons: Add Log & Export PDF / Excel / Text ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddLogDialog = true },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LocalGasStation, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت سوخت‌گیری", fontSize = 12.sp)
                    }

                    if (onNavigateToReminders != null) {
                        OutlinedButton(
                            onClick = onNavigateToReminders,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("محاسبه‌گر", fontSize = 11.sp)
                        }
                    }
                }
            }

            // --- Car Dossier & Comprehensive Reports (PDF/Excel) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text(text = "پرونده و گزارشات جامع خودرو", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "خروجی PDF و اکسل از سوابق مالی و فنی", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { fuelViewModel.exportPdf(context) },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("خروجی PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { fuelViewModel.exportExcel(context) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("اکسل (CSV)", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // --- Fuel Logs History List Widget (ویجت لیست تاریخچه سوخت‌گیری قابل اسکرول) ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fuel_history_widget_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
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
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0284C7).copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "ویجت تاریخچه سوخت‌گیری‌ها (شمسی)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "لیست قابل اسکرول سوابق سوخت‌گیری با تاریخ شمسی و لیتر",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "${filteredLogs.size} مورد",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFF334155))

                        if (filteredLogs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "هنوز سابقه سوخت‌گیری برای این خودرو ثبت نشده است.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // Dedicated scrollable container with a fixed max height
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(bottom = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredLogs.forEach { log ->
                                    FuelLogItem(
                                        log = log,
                                        onDelete = { logToDelete = log }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Add Vehicle Dialog (Car / Motorcycle) ---
    if (showAddVehicleDialog) {
        VehicleFormDialog(
            vehicleToEdit = null,
            onDismiss = { showAddVehicleDialog = false },
            onConfirm = { title, f2, letter, l3, city, fuelType, cap, odo, vType ->
                fuelViewModel.addVehicle(title, f2, letter, l3, city, fuelType, cap, odo, vType)
                showAddVehicleDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("$vType با موفقیت اضافه شد")
            }
        )
    }

    // --- Edit Vehicle Dialog (Car / Motorcycle) ---
    if (vehicleToEdit != null) {
        VehicleFormDialog(
            vehicleToEdit = vehicleToEdit,
            onDismiss = { vehicleToEdit = null },
            onConfirm = { title, f2, letter, l3, city, fuelType, cap, odo, vType ->
                val updated = vehicleToEdit!!.copy(
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
                vehicleToEdit = null
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("اطلاعات $vType با موفقیت ویرایش شد")
            }
        )
    }

    // --- Add Fuel Log Dialog ---
    if (showAddLogDialog && activeVehicle != null) {
        AddFuelLogDialog(
            vehicle = activeVehicle,
            onDismiss = { showAddLogDialog = false },
            onConfirm = { odo, liters, price, station, isFull, notes ->
                fuelViewModel.addFuelLog(activeVehicle.id, odo, liters, price, station, isFull, notes)
                showAddLogDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("سوابق سوخت‌گیری ذخیره شد")
            }
        )
    }

    // --- Delete Vehicle Confirmation (Custom Themed Dialog) ---
    if (vehicleToDelete != null) {
        com.angelgirlbrand.modiratsokhtandestelam.ui.components.CustomThemedConfirmDialog(
            onDismissRequest = { vehicleToDelete = null },
            title = "حذف وسیله نقلیه",
            message = "آیا از حذف «${vehicleToDelete!!.title}» و کلیه سوابق مرتبط با آن اطمینان دارید؟",
            confirmText = "حذف خودرو",
            dismissText = "انصراف",
            isDestructive = true,
            onConfirm = {
                fuelViewModel.deleteVehicle(vehicleToDelete!!)
                vehicleToDelete = null
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("خودرو با موفقیت حذف شد")
            }
        )
    }

    // --- Delete Log Confirmation (Custom Themed Dialog) ---
    if (logToDelete != null) {
        com.angelgirlbrand.modiratsokhtandestelam.ui.components.CustomThemedConfirmDialog(
            onDismissRequest = { logToDelete = null },
            title = "حذف رکورد سوخت",
            message = "آیا از حذف این رکورد سوخت‌گیری اطمینان دارید؟ این عملیات قابل بازگشت نیست.",
            confirmText = "حذف رکورد",
            dismissText = "انصراف",
            isDestructive = true,
            onConfirm = {
                fuelViewModel.deleteFuelLog(logToDelete!!)
                logToDelete = null
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("رکورد سوخت حذف شد")
            }
        )
    }

    // --- Quick Add Reminder Dialog ---
    if (showQuickAddReminderDialog && activeVehicle != null) {
        QuickAddReminderDialog(
            vehicle = activeVehicle,
            onDismiss = { showQuickAddReminderDialog = false },
            onConfirm = { serviceType, targetOdo, notes ->
                fuelViewModel.addServiceReminder(
                    vehicleId = activeVehicle.id,
                    serviceType = serviceType,
                    targetDateMillis = System.currentTimeMillis() + (90L * 24 * 3600 * 1000L),
                    targetOdometer = targetOdo,
                    notes = notes
                )
                showQuickAddReminderDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("یادآور سرویس ثبت شد و اعلان فعال گردید")
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun FuelLogItem(
    log: FuelLogEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, Color(0xFF334155))
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
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "%.1f لیتر ⛽".format(log.liters),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFBBF24),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "تاریخ: ${PersianDateHelper.toPersianDate(log.dateMillis)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "کیلومتر: %,d | قیمت لیتر: %,d | کل: %,d تومان".format(log.odometer, log.pricePerLiter, log.totalCost),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    if (log.stationName.isNotBlank()) {
                        Text(
                            text = "جایگاه سوخت: ${log.stationName}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "حذف رکورد سوخت",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddFuelLogDialog(
    vehicle: VehicleEntity,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, Long, String, Boolean, String) -> Unit
) {
    var odometer by remember { mutableStateOf(vehicle.currentOdometer.toString()) }
    var liters by remember { mutableStateOf("30") }
    var pricePerLiter by remember { mutableStateOf("1500") } // سهمیه‌ای ۱۵۰۰ یا آزاد ۳۰۰۰
    var stationName by remember { mutableStateOf("") }
    var isFullTank by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    var showScanner by remember { mutableStateOf(false) }

    if (showScanner) {
        CameraQrScannerDialog(
            onDismiss = { showScanner = false },
            onReceiptScanned = { sLiters, sPrice, sStation, sNotes ->
                liters = sLiters.toString()
                pricePerLiter = sPrice.toString()
                if (sStation.isNotBlank()) {
                    stationName = sStation
                }
                if (sNotes.isNotBlank()) {
                    notes = sNotes
                }
                showScanner = false
            }
        )
    }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "ثبت سوخت‌گیری جدید",
        subtitle = "خودرو ${vehicle.title}",
        icon = Icons.Default.LocalGasStation,
        iconTint = Color(0xFFF97316)
    ) {
        // Receipt Scan Button
        Button(
            onClick = { showScanner = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFFF97316)),
            modifier = Modifier.fillMaxWidth().testTag("scan_receipt_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFFF97316))
            Spacer(modifier = Modifier.width(8.dp))
            Text("اسکن هوشمند رسید سوخت‌گیری 📸", color = Color(0xFFF97316), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = odometer,
            onValueChange = { odometer = it },
            label = { Text("کیلومترشمار فعلی خودرو") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = liters,
                onValueChange = { liters = it },
                label = { Text("میزان بنزین (لیتر)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = pricePerLiter,
                onValueChange = { pricePerLiter = it },
                label = { Text("قیمت هر لیتر (تومان)") },
                modifier = Modifier.weight(1.2f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Quick price chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AssistChip(
                onClick = { pricePerLiter = "1500" },
                label = { Text("سهمیه‌ای (۱,۵۰۰)") }
            )
            AssistChip(
                onClick = { pricePerLiter = "3000" },
                label = { Text("آزاد (۳,۰۰۰)") }
            )
        }

        OutlinedTextField(
            value = stationName,
            onValueChange = { stationName = it },
            label = { Text("نام یا نشانی جایگاه سوخت (اختیاری)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isFullTank, onCheckedChange = { isFullTank = it })
            Spacer(modifier = Modifier.width(6.dp))
            Text("باک کاملا پر شد", style = MaterialTheme.typography.bodyMedium)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val odo = odometer.toIntOrNull() ?: vehicle.currentOdometer
                    val lit = liters.toDoubleOrNull() ?: 1.0
                    val prc = pricePerLiter.toLongOrNull() ?: 1500L
                    onConfirm(odo, lit, prc, stationName, isFullTank, notes)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ثبت و تایید", fontWeight = FontWeight.Bold)
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

@Composable
private fun QuickAddReminderDialog(
    vehicle: VehicleEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    val presets = listOf(
        "تعویض روغن موتور",
        "تعویض فیلتر روغن و هوا",
        "تعویض لنت ترمز",
        "تعویض تسمه تایم",
        "شمع و وایر",
        "سرویس دوره‌ای جامع"
    )

    var selectedService by remember { mutableStateOf(presets[0]) }
    var targetOdometer by remember { mutableStateOf((vehicle.currentOdometer + 5000).toString()) }
    var notes by remember { mutableStateOf("") }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "افزودن یادآور سرویس",
        subtitle = "خودرو ${vehicle.title}",
        icon = Icons.Default.NotificationsActive,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Text("انتخاب سرویس:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                presets.take(2).forEach { p ->
                    FilterChip(
                        selected = selectedService == p,
                        onClick = { selectedService = p },
                        label = { Text(p, fontSize = 11.sp) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                presets.drop(2).take(2).forEach { p ->
                    FilterChip(
                        selected = selectedService == p,
                        onClick = { selectedService = p },
                        label = { Text(p, fontSize = 11.sp) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = targetOdometer,
            onValueChange = { targetOdometer = it },
            label = { Text("کیلومتر هدف (فعلی: ${vehicle.currentOdometer})") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("توضیحات (اختیاری)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val odo = targetOdometer.toIntOrNull() ?: (vehicle.currentOdometer + 5000)
                    onConfirm(selectedService, odo, notes)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ثبت یادآور", fontWeight = FontWeight.Bold)
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
