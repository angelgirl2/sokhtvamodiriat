package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.FuelConsumptionChart
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.IranianPlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.VehicleOverviewCard
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FuelManagementScreen(
    fuelViewModel: FuelViewModel,
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

    var showAddVehicleDialog by remember { mutableStateOf(false) }
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
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
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
                                Toast.makeText(context, "درخواست تمدید بیمه در بخش «درخواست خدمات» آماده ثبت است", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onToggleReminderComplete = { reminder ->
                            fuelViewModel.toggleReminderCompleted(reminder)
                        },
                        onUpdateInsurance = { expiryMillis, company, type ->
                            fuelViewModel.updateVehicleInsurance(activeVehicle, expiryMillis, company, type)
                            Toast.makeText(context, "اطلاعات بیمه‌نامه ذخیره شد", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteVehicleClick = { vehicleToDelete = activeVehicle },
                        onNavigateToReminders = onNavigateToReminders
                    )
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

            // --- Export and Archive Card ---
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("بایگانی و صدور گزارش:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { fuelViewModel.exportPdf(context) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF", fontSize = 10.5.sp)
                            }

                            FilledTonalButton(
                                onClick = { fuelViewModel.shareReportText(context) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ارسال متن", fontSize = 10.5.sp)
                            }

                            FilledTonalButton(
                                onClick = { fuelViewModel.copyReportText(context) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // --- Fuel Logs History List ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تاریخچه سوخت‌گیری‌های اخیر",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredLogs.size} مورد",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (filteredLogs.isEmpty()) {
                item {
                    Text(
                        text = "هنوز سابقه سوخت‌گیری برای این خودرو ثبت نشده است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(filteredLogs) { log ->
                    FuelLogItem(
                        log = log,
                        onDelete = { logToDelete = log }
                    )
                }
            }
        }
    }

    // --- Add Vehicle Dialog ---
    if (showAddVehicleDialog) {
        AddVehicleDialog(
            onDismiss = { showAddVehicleDialog = false },
            onConfirm = { title, f2, letter, l3, city, fuelType, cap, odo ->
                fuelViewModel.addVehicle(title, f2, letter, l3, city, fuelType, cap, odo)
                showAddVehicleDialog = false
                Toast.makeText(context, "خودرو با موفقیت اضافه شد", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "سوابق سوخت‌گیری ذخیره شد", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- Delete Vehicle Confirmation ---
    if (vehicleToDelete != null) {
        AlertDialog(
            onDismissRequest = { vehicleToDelete = null },
            title = { Text("حذف خودرو") },
            text = { Text("آیا از حذف خودرو «${vehicleToDelete!!.title}» و کلیه سوابق آن اطمینان دارید؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        fuelViewModel.deleteVehicle(vehicleToDelete!!)
                        vehicleToDelete = null
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { vehicleToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    // --- Delete Log Confirmation ---
    if (logToDelete != null) {
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("حذف رکورد سوخت") },
            text = { Text("آیا از حذف این رکورد سوخت‌گیری اطمینان دارید؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        fuelViewModel.deleteFuelLog(logToDelete!!)
                        logToDelete = null
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("انصراف")
                }
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
                Toast.makeText(context, "یادآور سرویس ثبت شد و اعلان فعال گردید", Toast.LENGTH_SHORT).show()
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
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "%.1f لیتر | %,d تومان".format(log.liters, log.totalCost),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "کیلومتر: %,d | تاریخ: %s".format(log.odometer, dateFormat.format(Date(log.dateMillis))),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (log.stationName.isNotBlank()) {
                        Text(
                            text = "جایگاه: ${log.stationName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, Double, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var plateF2 by remember { mutableStateOf("") }
    var plateLetter by remember { mutableStateOf("ب") }
    var plateL3 by remember { mutableStateOf("") }
    var plateCity by remember { mutableStateOf("11") }
    var fuelType by remember { mutableStateOf("بنزین معمولی") }
    var tankCapacity by remember { mutableStateOf("50") }
    var currentOdometer by remember { mutableStateOf("0") }

    val letters = listOf("ب", "ج", "د", "س", "ص", "ط", "ق", "ل", "م", "ن", "و", "هـ", "ی")
    val fuelTypes = listOf("بنزین معمولی", "بنزین سوپر", "دوگانه‌سوز CNG", "گازوئیل")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ثبت خودرو جدید",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("نام یا مدل خودرو (مثلا: دنا پلاس، پژو ۲۰۶)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Iranian Plate Interactive Input
                Text(
                    text = "شماره پلاک ملی:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = plateCity,
                        onValueChange = { if (it.length <= 2) plateCity = it },
                        label = { Text("کد شهر") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = plateL3,
                        onValueChange = { if (it.length <= 3) plateL3 = it },
                        label = { Text("۳ رقم") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    // Letter picker
                    Box(modifier = Modifier.weight(1f)) {
                        var expanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(plateLetter, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            letters.forEach { l ->
                                DropdownMenuItem(
                                    text = { Text(l) },
                                    onClick = {
                                        plateLetter = l
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = plateF2,
                        onValueChange = { if (it.length <= 2) plateF2 = it },
                        label = { Text("۲ رقم") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Fuel Type and Tank Capacity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tankCapacity,
                        onValueChange = { tankCapacity = it },
                        label = { Text("حجم باک (لیتر)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = currentOdometer,
                        onValueChange = { currentOdometer = it },
                        label = { Text("کیلومتر فعلی") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(
                                    title,
                                    plateF2.ifEmpty { "11" },
                                    plateLetter,
                                    plateL3.ifEmpty { "111" },
                                    plateCity.ifEmpty { "11" },
                                    fuelType,
                                    tankCapacity.toDoubleOrNull() ?: 50.0,
                                    currentOdometer.toIntOrNull() ?: 0
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ثبت و ذخیره")
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ثبت سوخت‌گیری برای ${vehicle.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = odometer,
                    onValueChange = { odometer = it },
                    label = { Text("کیلومترشمار فعلی خودرو") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pricePerLiter,
                        onValueChange = { pricePerLiter = it },
                        label = { Text("قیمت هر لیتر (تومان)") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
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
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isFullTank, onCheckedChange = { isFullTank = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("باک کاملا پر شد", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ثبت و تایید")
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "افزودن یادآور سرویس برای ${vehicle.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

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
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات (اختیاری)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ثبت یادآور")
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
