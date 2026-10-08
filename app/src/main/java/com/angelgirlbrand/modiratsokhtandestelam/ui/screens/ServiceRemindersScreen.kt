package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceHistoryEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceReminderEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.M3ThemedDialogContainer
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel
import com.angelgirlbrand.modiratsokhtandestelam.util.PersianDateHelper
import java.text.SimpleDateFormat
import java.util.*

enum class ServiceTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HISTORY("تاریخچه سرویس‌ها", Icons.Default.History),
    REMINDERS("یادآورهای فعال", Icons.Default.NotificationsActive),
    CONSUMABLES("قطعات مصرفی", Icons.Default.Build),
    CALCULATOR("محاسبه‌گر سوخت", Icons.Default.Calculate)
}

@Composable
fun ServiceRemindersScreen(
    fuelViewModel: FuelViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reminders by fuelViewModel.reminders.collectAsState()
    val serviceHistory by fuelViewModel.serviceHistory.collectAsState()
    val vehicles by fuelViewModel.vehicles.collectAsState()
    val selectedVehicleId by fuelViewModel.selectedVehicleId.collectAsState()
    val fuelLogs by fuelViewModel.fuelLogs.collectAsState()

    var selectedTab by remember { mutableStateOf(ServiceTab.HISTORY) }
    var showAddHistoryDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showEditInspectionDialog by remember { mutableStateOf(false) }

    val activeVehicle = remember(vehicles, selectedVehicleId) {
        vehicles.firstOrNull { it.id == selectedVehicleId } ?: vehicles.firstOrNull()
    }

    val vehicleHistory = remember(serviceHistory, activeVehicle) {
        if (activeVehicle != null) {
            serviceHistory.filter { it.vehicleId == activeVehicle.id }
        } else {
            serviceHistory
        }
    }

    val vehicleReminders = remember(reminders, activeVehicle) {
        if (activeVehicle != null) {
            reminders.filter { it.vehicleId == activeVehicle.id }
        } else {
            reminders
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- Screen Header ---
        Text(
            text = "سرویس، نگهداری و محاسبه‌گر خودرو",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "ثبت تعویض روغن و فیلترها، یادآورهای دوره‌ای و محاسبه دقیق مصرف سوخت",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- Active Vehicle Selector Chip (If multiple vehicles) ---
        if (vehicles.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(vehicles) { v ->
                    val isSelected = (activeVehicle?.id == v.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = { fuelViewModel.selectVehicle(v.id) },
                        label = {
                            Text(
                                text = "${v.title} (${v.formattedPlate})",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- Segmented 3-Tab Selector ---
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
        ) {
            ServiceTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(tab.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Tab Content ---
        when (selectedTab) {
            ServiceTab.HISTORY -> {
                ServiceHistoryTabContent(
                    activeVehicle = activeVehicle,
                    historyList = vehicleHistory,
                    onAddNewClick = {
                        if (activeVehicle == null) {
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("ابتدا در تب سوخت یک خودرو اضافه کنید")
                        } else {
                            showAddHistoryDialog = true
                        }
                    },
                    onExportPdf = { fuelViewModel.exportPdf(context) },
                    onShareText = { fuelViewModel.shareReportText(context) },
                    onDeleteHistory = { fuelViewModel.deleteServiceHistory(it) }
                )
            }
            ServiceTab.REMINDERS -> {
                ServiceRemindersTabContent(
                    activeVehicle = activeVehicle,
                    remindersList = vehicleReminders,
                    onAddNewClick = {
                        if (activeVehicle == null) {
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("ابتدا در تب سوخت یک خودرو اضافه کنید")
                        } else {
                            showAddReminderDialog = true
                        }
                    },
                    onEditInspectionClick = {
                        if (activeVehicle == null) {
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("ابتدا در تب سوخت یک خودرو اضافه کنید")
                        } else {
                            showEditInspectionDialog = true
                        }
                    },
                    onTriggerInspectionAlert = {
                        if (activeVehicle != null) {
                            fuelViewModel.updateVehicleInspection(
                                vehicle = activeVehicle,
                                expiryMillis = activeVehicle.effectiveInspectionExpiryMillis,
                                centerName = activeVehicle.inspectionCenter.ifBlank { "مرکز مکانیزه معاینه فنی بیهقی" },
                                notifyDaysBefore = activeVehicle.inspectionNotifyDaysBefore
                            )
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("اعلان و یادآور معاینه فنی با موفقیت ارسال و تنظیم شد")
                        }
                    },
                    onToggleComplete = { fuelViewModel.toggleReminderCompleted(it) },
                    onDeleteReminder = { fuelViewModel.deleteReminder(it) }
                )
            }
            ServiceTab.CONSUMABLES -> {
                ConsumablesTabContent(
                    activeVehicle = activeVehicle
                )
            }
            ServiceTab.CALCULATOR -> {
                FuelCalculatorTabContent(
                    activeVehicle = activeVehicle,
                    recentLogs = fuelLogs
                )
            }
        }
    }

    // --- Dialog: Register Periodic Service History ---
    if (showAddHistoryDialog && activeVehicle != null) {
        AddServiceHistoryDialog(
            vehicle = activeVehicle,
            onDismiss = { showAddHistoryDialog = false },
            onConfirm = { type, items, odo, nextOdo, date, cost, shop, notes ->
                fuelViewModel.addServiceHistory(
                    vehicleId = activeVehicle.id,
                    serviceType = type,
                    itemsChanged = items,
                    odometer = odo,
                    nextDueOdometer = nextOdo,
                    dateMillis = date,
                    cost = cost,
                    mechanicOrShop = shop,
                    notes = notes
                )
                showAddHistoryDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("سرویس با موفقیت در تاریخچه خودرو ثبت شد")
            }
        )
    }

    // --- Dialog: Add Reminder ---
    if (showAddReminderDialog && activeVehicle != null) {
        AddReminderDialog(
            vehicle = activeVehicle,
            onDismiss = { showAddReminderDialog = false },
            onConfirm = { serviceType, targetOdo, notes ->
                fuelViewModel.addServiceReminder(
                    vehicleId = activeVehicle.id,
                    serviceType = serviceType,
                    targetDateMillis = System.currentTimeMillis() + (30L * 24 * 3600 * 1000),
                    targetOdometer = targetOdo,
                    notes = notes
                )
                showAddReminderDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("یادآور جدید با موفقیت فعال شد")
            }
        )
    }

    // --- Dialog: Edit Technical Inspection Reminder ---
    if (showEditInspectionDialog && activeVehicle != null) {
        EditInspectionDialog(
            vehicle = activeVehicle,
            onDismiss = { showEditInspectionDialog = false },
            onConfirm = { expiryMillis, centerName, notifyDays ->
                fuelViewModel.updateVehicleInspection(
                    vehicle = activeVehicle,
                    expiryMillis = expiryMillis,
                    centerName = centerName,
                    notifyDaysBefore = notifyDays
                )
                showEditInspectionDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("اطلاعات و یادآور معاینه فنی با موفقیت ذخیره و فعال شد")
            }
        )
    }
}

// =======================================================================================
// TAB 1: SERVICE HISTORY CONTENT
// =======================================================================================
@Composable
private fun ServiceHistoryTabContent(
    activeVehicle: VehicleEntity?,
    historyList: List<ServiceHistoryEntity>,
    onAddNewClick: () -> Unit,
    onExportPdf: () -> Unit = {},
    onShareText: () -> Unit = {},
    onDeleteHistory: (ServiceHistoryEntity) -> Unit
) {
    val totalCost = historyList.sumOf { it.cost }
    val lastService = historyList.firstOrNull()

    var searchQuery by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    val filteredHistory = remember(historyList, searchQuery) {
        if (searchQuery.isBlank()) {
            historyList
        } else {
            val query = searchQuery.trim().lowercase()
            historyList.filter { service ->
                val dateStr = PersianDateHelper.toPersianDate(service.dateMillis)
                service.serviceType.lowercase().contains(query) ||
                service.itemsChanged.lowercase().contains(query) ||
                service.mechanicOrShop.lowercase().contains(query) ||
                service.notes.lowercase().contains(query) ||
                service.cost.toString().contains(query) ||
                service.odometer.toString().contains(query) ||
                dateStr.contains(query)
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Summary & Action Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("service_history_summary_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Column {
                            Text(
                                text = "ثبت و دفترچه سرویس‌های دوره‌ای",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (activeVehicle != null) "خودرو: ${activeVehicle.title} (${activeVehicle.formattedPlate})" else "ثبت تعویض روغن، فیلتر و قطعات مصرفی",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onAddNewClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ثبت سرویس جدید", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Stat Metrics Box
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("تعداد سرویس‌ها", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${historyList.size} مورد", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("مجموع هزینه‌ها", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("%,d تومان".format(totalCost), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("آخرین تعویض", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (lastService != null) "%,d km".format(lastService.odometer) else "---",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                    }

                    // Export & Share Quick Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExportPdf,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("خروجی PDF", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onShareText,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ارسال به پیام‌رسان", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Smart Search Bar
        if (historyList.isNotEmpty()) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_history_search_input"),
                    placeholder = { Text("جستجوی هوشمند (قطعه، روغن، تاریخ، تعمیرگاه...)", fontSize = 11.5.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "پاک کردن",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سوابق نگهداری و تعویض قطعات",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (searchQuery.isBlank()) "${historyList.size} رکورد ثبت شده" else "${filteredHistory.size} مورد یافت شد",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (filteredHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.BuildCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "هیچ سرویسی با این عنوان یافت نشد" else "هنوز سابقه‌ای برای این خودرو ثبت نشده است",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "عبارت جستجو را تغییر دهید یا عبارت دیگری را امتحان کنید." else "پس از هر بار تعویض روغن موتور، فیلترها، لنت، شمع یا تسمه، اطلاعات آن را با زدن دکمه «ثبت سرویس جدید» وارد کنید تا تاریخچه دقیق خودرو همیشه همراهتان باشد.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(filteredHistory) { item ->
                ServiceHistoryItemCard(
                    record = item,
                    onDelete = { onDeleteHistory(item) }
                )
            }
        }
    }
}

@Composable
private fun ServiceHistoryItemCard(
    record: ServiceHistoryEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SettingsSuggest,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = record.serviceType,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تاریخ: ${PersianDateHelper.toPersianDate(record.dateMillis)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

            // Items changed badge/text
            if (record.itemsChanged.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اقلام تعویض شده: ${record.itemsChanged}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Odometer & Next Due & Cost Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "کارکرد: %,d کیلومتر".format(record.odometer),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (record.nextDueOdometer > 0) {
                        Text(
                            text = "سرویس بعدی: %,d کیلومتر".format(record.nextDueOdometer),
                            fontSize = 10.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (record.cost > 0) "%,d تومان".format(record.cost) else "رایگان / گارانتی",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (record.mechanicOrShop.isNotBlank()) {
                        Text(
                            text = record.mechanicOrShop,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (record.notes.isNotBlank()) {
                Text(
                    text = "یادداشت: ${record.notes}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =======================================================================================
// TAB 2: REMINDERS TAB CONTENT
// =======================================================================================
@Composable
private fun ServiceRemindersTabContent(
    activeVehicle: VehicleEntity?,
    remindersList: List<ServiceReminderEntity>,
    onAddNewClick: () -> Unit,
    onEditInspectionClick: () -> Unit = {},
    onTriggerInspectionAlert: () -> Unit = {},
    onToggleComplete: (ServiceReminderEntity) -> Unit,
    onDeleteReminder: (ServiceReminderEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. Prominent Technical Inspection (معاینه فنی) Alert & Reminder Card ---
        if (activeVehicle != null) {
            item {
                TechnicalInspectionCard(
                    vehicle = activeVehicle,
                    onEditClick = onEditInspectionClick,
                    onTriggerAlert = onTriggerInspectionAlert
                )
            }
        }

        // --- 2. Periodic Service Reminders Header ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminders_header_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AddAlarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "یادآورهای سرویس و تعویض قطعات",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "هشدار خودکار موعد تعویض روغن، فیلترها، لنت و تسمه تایم",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onAddNewClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("add_reminder_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddAlarm, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ثبت یادآور جدید", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (remindersList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.AlarmOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "هیچ یادآور فعالی وجود ندارد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "می‌توانید برای کیلومترهای آینده هشدار تعویض روغن یا تسمه تایم تنظیم کنید تا در موعد مقرر مطلع شوید.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = onAddNewClick,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddAlarm, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ثبت اولین یادآور", fontSize = 11.5.sp)
                        }
                    }
                }
            }
        } else {
            items(remindersList) { reminder ->
                val currentOdo = activeVehicle?.currentOdometer ?: 0
                val remainingKm = reminder.targetOdometer - currentOdo

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (reminder.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = reminder.isCompleted,
                                onCheckedChange = { onToggleComplete(reminder) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = reminder.serviceType,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (reminder.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "کیلومتر هدف: %,d کیلومتر".format(reminder.targetOdometer),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (!reminder.isCompleted && remainingKm > 0) {
                                    Text(
                                        text = "مانده تا سرویس: %,d کیلومتر".format(remainingKm),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remainingKm < 1000) Color(0xFFEF4444) else Color(0xFF16A34A)
                                    )
                                } else if (!reminder.isCompleted && remainingKm <= 0) {
                                    Text(
                                        text = "⚠️ موعد سرویس فرا رسیده است!",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                                if (reminder.notes.isNotBlank()) {
                                    Text(
                                        text = reminder.notes,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { onDeleteReminder(reminder) }) {
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
        }
    }
}

// =======================================================================================
// TAB: INTERNAL CONSUMABLE PARTS CHECKLIST & REPAIRS
// =======================================================================================
@Composable
private fun ConsumablesTabContent(
    activeVehicle: VehicleEntity?
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("consumables_prefs", Context.MODE_PRIVATE) }
    val vehicleId = activeVehicle?.id ?: -1L

    val defaultParts = listOf(
        "روغن موتور",
        "فیلتر روغن",
        "فیلتر هوا",
        "فیلتر کابین",
        "فیلتر بنزین",
        "شمع موتور",
        "تسمه تایم",
        "تسمه دینام",
        "لنت ترمز جلو",
        "لنت ترمز عقب",
        "روغن گیربکس",
        "روغن هیدرولیک فرمان",
        "ضدیخ و مایع خنک‌کننده",
        "لاستیک‌ها",
        "باتری"
    )

    var customPartsList by remember(vehicleId) {
        mutableStateOf(
            prefs.getStringSet("custom_parts_$vehicleId", emptySet())?.toList() ?: emptyList()
        )
    }

    val allParts = defaultParts + customPartsList

    var showAddCustomPartDialog by remember { mutableStateOf(false) }
    var selectedPartToEdit by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Tab Title and Description
        Text(
            text = "📋 چک‌لیست وضعیت و سرویس قطعات مصرفی داخلی",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "در این قسمت می‌توانید وضعیت تعویض یا تعمیر قطعات مصرفی داخلی خودرو را به صورت چک‌لیست پایش کنید. تمام جزئیات راست‌چین شده‌اند.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Add Custom Consumable Button
        Button(
            onClick = {
                if (activeVehicle == null) {
                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("ابتدا یک خودرو انتخاب یا اضافه کنید")
                } else {
                    showAddCustomPartDialog = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("افزودن قطعه مصرفی دلخواه 🔧", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeVehicle == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "جهت ثبت چک‌لیست قطعات مصرفی، ابتدا یک خودرو انتخاب کنید.",
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(allParts) { partName ->
                        val isCheckedKey = "checked_${vehicleId}_$partName"
                        val actionKey = "action_${vehicleId}_$partName"
                        val odoKey = "odo_${vehicleId}_$partName"
                        val dateKey = "date_${vehicleId}_$partName"
                        val notesKey = "notes_${vehicleId}_$partName"

                        var isChecked by remember(isCheckedKey) {
                            mutableStateOf(prefs.getBoolean(isCheckedKey, false))
                        }
                        val action = prefs.getString(actionKey, "نیاز به بررسی") ?: "نیاز به بررسی"
                        val odo = prefs.getString(odoKey, "") ?: ""
                        val date = prefs.getString(dateKey, "") ?: ""
                        val notes = prefs.getString(notesKey, "") ?: ""

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPartToEdit = partName },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChecked) Color(0xFF0F172A) else Color(0xFF1E293B)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isChecked) Color(0xFF16A34A).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left side: Edit Icon
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "ویرایش",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )

                                // Right side: Checkbox & Texts (Right-Aligned)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Badge / Status
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (action) {
                                                    "تعویض شده" -> Color(0xFF16A34A).copy(alpha = 0.15f)
                                                    "تعمیر شده" -> Color(0xFF0284C7).copy(alpha = 0.15f)
                                                    else -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                                },
                                                border = BorderStroke(
                                                    0.8.dp,
                                                    when (action) {
                                                        "تعویض شده" -> Color(0xFF16A34A)
                                                        "تعمیر شده" -> Color(0xFF0284C7)
                                                        else -> Color(0xFFEF4444)
                                                    }
                                                )
                                            ) {
                                                Text(
                                                    text = action,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (action) {
                                                        "تعویض شده" -> Color(0xFF4ADE80)
                                                        "تعمیر شده" -> Color(0xFF38BDF8)
                                                        else -> Color(0xFFF87171)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = partName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = Color.White,
                                                textAlign = TextAlign.Right
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        if (odo.isNotBlank() || date.isNotBlank()) {
                                            Text(
                                                text = "اقدام: " + (if (date.isNotBlank()) "تاریخ $date" else "") +
                                                        (if (odo.isNotBlank()) " | کیلومتر $odo" else ""),
                                                fontSize = 11.sp,
                                                color = Color(0xFF38BDF8),
                                                textAlign = TextAlign.Right
                                            )
                                        }

                                        if (notes.isNotBlank()) {
                                            Text(
                                                text = "توضیحات: $notes",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8),
                                                textAlign = TextAlign.Right,
                                                lineHeight = 15.sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            Text(
                                                text = "بدون توضیحات ثبت‌شده",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B),
                                                textAlign = TextAlign.Right
                                            )
                                        }
                                    }

                                    // Right-most Checkbox
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            prefs.edit().putBoolean(isCheckedKey, checked).apply()
                                            isChecked = checked
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // A) Dialog: Add Custom Part
    if (showAddCustomPartDialog) {
        var customPartName by remember { mutableStateOf("") }
        M3ThemedDialogContainer(
            onDismissRequest = { showAddCustomPartDialog = false },
            title = "افزودن قطعه مصرفی جدید",
            subtitle = "ثبت در چک‌لیست قطعات و سرویس خودرو",
            icon = Icons.Default.AddCircleOutline
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "نام قطعه مصرفی مورد نظر خود را بنویسید (مانند: شیشه‌شور، تسمه کولر):",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                OutlinedTextField(
                    value = customPartName,
                    onValueChange = { customPartName = it },
                    label = { Text("نام قطعه") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showAddCustomPartDialog = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }
                    Button(
                        onClick = {
                            val nameClean = customPartName.trim()
                            if (nameClean.isNotBlank()) {
                                val updatedSet = (customPartsList + nameClean).toSet()
                                prefs.edit().putStringSet("custom_parts_$vehicleId", updatedSet).apply()
                                customPartsList = updatedSet.toList()
                                showAddCustomPartDialog = false
                                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("قطعه «$nameClean» با موفقیت اضافه شد")
                            } else {
                                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("لطفا نام قطعه را وارد کنید")
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("افزودن به لیست", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // B) Dialog: Edit Consumable Part Status and Details
    if (selectedPartToEdit != null) {
        val partName = selectedPartToEdit!!
        val isCheckedKey = "checked_${vehicleId}_$partName"
        val actionKey = "action_${vehicleId}_$partName"
        val odoKey = "odo_${vehicleId}_$partName"
        val dateKey = "date_${vehicleId}_$partName"
        val notesKey = "notes_${vehicleId}_$partName"

        var isChecked by remember { mutableStateOf(prefs.getBoolean(isCheckedKey, false)) }
        var actionState by remember { mutableStateOf(prefs.getString(actionKey, "نیاز به بررسی") ?: "نیاز به بررسی") }
        var odoState by remember { mutableStateOf(prefs.getString(odoKey, "") ?: "") }
        var dateState by remember { mutableStateOf(prefs.getString(dateKey, "") ?: "") }
        var notesState by remember { mutableStateOf(prefs.getString(notesKey, "") ?: "") }

        M3ThemedDialogContainer(
            onDismissRequest = { selectedPartToEdit = null },
            title = "ویرایش وضعیت قطعه: $partName",
            subtitle = "ثبت آخرین اقدام، کیلومتر و تاریخ سرویس",
            icon = Icons.Default.Settings
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Checkbox Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .clickable { isChecked = !isChecked }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تعویض/تعمیر اخیر انجام شده است (چک‌لیست)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Checkbox(checked = isChecked, onCheckedChange = { isChecked = it })
                }

                Text(
                    text = "نوع اقدام انجام‌شده:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Triple Option Choice Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("تعویض شده", "تعمیر شده", "نیاز به بررسی").forEach { option ->
                        FilterChip(
                            selected = actionState == option,
                            onClick = { actionState = option },
                            label = { Text(option, fontSize = 10.5.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = odoState,
                    onValueChange = { odoState = it },
                    label = { Text("کیلومتر کارکرد خودرو هنگام سرویس") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("مثلاً ۱۲۰,۰۰۰", fontSize = 11.sp) }
                )

                OutlinedTextField(
                    value = dateState,
                    onValueChange = { dateState = it },
                    label = { Text("تاریخ آخرین تعویض یا تعمیر") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("مثلاً ۱۴۰۲/۰۷/۱۰", fontSize = 11.sp) }
                )

                OutlinedTextField(
                    value = notesState,
                    onValueChange = { notesState = it },
                    label = { Text("توضیحات، جزئیات و برند قطعه") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("مثلاً لنت ایساکو اصل، تعویض در کیلومتر ۱۱۰ هزار", fontSize = 11.sp) },
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { selectedPartToEdit = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }
                    Button(
                        onClick = {
                            prefs.edit()
                                .putBoolean(isCheckedKey, isChecked)
                                .putString(actionKey, actionState)
                                .putString(odoKey, odoState.trim())
                                .putString(dateKey, dateState.trim())
                                .putString(notesKey, notesState.trim())
                                .apply()
                            selectedPartToEdit = null
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("اطلاعات قطعه «$partName» ذخیره شد ✅")
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ذخیره تغییرات", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =======================================================================================
// TAB 3: FUEL CONSUMPTION CALCULATOR
// =======================================================================================
@Composable
private fun FuelCalculatorTabContent(
    activeVehicle: VehicleEntity?,
    recentLogs: List<FuelLogEntity>
) {
    var distanceInput by remember { mutableStateOf("100") }
    var fuelInput by remember { mutableStateOf("7.5") }
    var pricePerLiterInput by remember { mutableStateOf("3000") } // 1500 (quota), 3000 (free)

    var customTripDistance by remember { mutableStateOf("450") } // e.g. 450 km trip estimation

    val distance = distanceInput.toDoubleOrNull() ?: 0.0
    val fuelLiters = fuelInput.toDoubleOrNull() ?: 0.0
    val pricePerLiter = pricePerLiterInput.toLongOrNull() ?: 3000L

    // Calculate L/100km and km/L
    val lPer100Km = remember(distance, fuelLiters) {
        if (distance > 0 && fuelLiters > 0) {
            (fuelLiters / distance) * 100.0
        } else {
            0.0
        }
    }

    val kmPerLiter = remember(distance, fuelLiters) {
        if (fuelLiters > 0 && distance > 0) {
            distance / fuelLiters
        } else {
            0.0
        }
    }

    val costPerKm = remember(lPer100Km, pricePerLiter) {
        if (lPer100Km > 0) {
            (lPer100Km / 100.0) * pricePerLiter
        } else {
            0.0
        }
    }

    // Custom Trip Estimation
    val tripDist = customTripDistance.toDoubleOrNull() ?: 0.0
    val tripFuelNeeded = remember(lPer100Km, tripDist) {
        if (lPer100Km > 0 && tripDist > 0) {
            (lPer100Km / 100.0) * tripDist
        } else {
            0.0
        }
    }
    val tripTotalCost = remember(tripFuelNeeded, pricePerLiter) {
        (tripFuelNeeded * pricePerLiter).toLong()
    }

    // Fuel Efficiency Status
    val (statusTitle, statusColor, statusDesc) = when {
        lPer100Km <= 0 -> Triple("اطلاعات را وارد کنید", MaterialTheme.colorScheme.onSurfaceVariant, "کیلومتر و لیتر مصرفی را وارد نمایید")
        lPer100Km < 6.0 -> Triple("فوق‌العاده اقتصادی و عالی 🟢", Color(0xFF16A34A), "مصرف خودرو در ایده‌آل‌ترین وضعیت اقتصادی قرار دارد.")
        lPer100Km <= 8.5 -> Triple("مصرف نرمال و استاندارد 🟡", Color(0xFF0284C7), "مصرف سوخت در محدوده استاندارد خودروهای شهری و جاده‌ای است.")
        lPer100Km <= 11.0 -> Triple("مصرف نسبتاً بالا 🟠", Color(0xFFF59E0B), "مصرف خودرو کمی بالاتر از حد بهینه است. فیلتر هوا و شمع‌ها بررسی شوند.")
        else -> Triple("پرمصرف - نیاز به بررسی فنی 🔴", Color(0xFFEF4444), "مصرف غیرعادی است. تنظیم موتور، تعویض سنسور اکسیژن یا شستشوی انژکتور توصیه می‌شود.")
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Calculation Results Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fuel_calculator_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "نتیجه محاسبه مصرف سوخت",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = statusTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    // Large Digital Metric Display
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (lPer100Km > 0) String.format(Locale.US, "%.1f", lPer100Km) else "---",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "لیتر در ۱۰۰ کیلومتر (L/100km)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = statusDesc,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Secondary Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("پیمایش به ازای ۱ لیتر", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (kmPerLiter > 0) String.format(Locale.US, "%.1f km", kmPerLiter) else "---",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7)
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("هزینه هر ۱ کیلومتر", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (costPerKm > 0) "%,d تومان".format(costPerKm.toLong()) else "---",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Inputs Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ورودی‌های محاسبه مصرف:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Distance Input
                    OutlinedTextField(
                        value = distanceInput,
                        onValueChange = { distanceInput = it },
                        label = { Text("مسافت پیموده شده (کیلومتر)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Timeline, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Quick Distance Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("100", "250", "500", "900").forEach { dist ->
                            OutlinedButton(
                                onClick = { distanceInput = dist },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("$dist km", fontSize = 10.sp)
                            }
                        }
                    }

                    // Fuel Liters Input
                    OutlinedTextField(
                        value = fuelInput,
                        onValueChange = { fuelInput = it },
                        label = { Text("سوخت مصرفی در این مسافت (لیتر)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.LocalGasStation, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Fuel Price Selection
                    Text(
                        text = "نرخ بنزین به ازای هر لیتر:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = pricePerLiterInput == "1500",
                            onClick = { pricePerLiterInput = "1500" },
                            label = { Text("۱,۵۰۰ تومان (سهمیه‌ای)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        FilterChip(
                            selected = pricePerLiterInput == "3000",
                            onClick = { pricePerLiterInput = "3000" },
                            label = { Text("۳,۰۰۰ تومان (آزاد)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Trip Fuel & Cost Estimator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "برآورد مصرف و هزینه سفر پیش‌رو",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "مسافت سفر بعدی خود را وارد کنید تا بر اساس میانگین مصرف بالا، لیتر بنزین مورد نیاز و هزینه کل محاسبه شود:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = customTripDistance,
                        onValueChange = { customTripDistance = it },
                        label = { Text("مسافت سفر (کیلومتر)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (tripDist > 0 && lPer100Km > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("بنزین مورد نیاز سفر:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(String.format(Locale.US, "%.1f لیتر", tripFuelNeeded), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("هزینه کل بنزین سفر:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("%,d تومان".format(tripTotalCost), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
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
// DIALOG: ADD PERIODIC SERVICE HISTORY
// =======================================================================================
@Composable
private fun AddServiceHistoryDialog(
    vehicle: VehicleEntity,
    onDismiss: () -> Unit,
    onConfirm: (type: String, items: String, odo: Int, nextOdo: Int, date: Long, cost: Long, shop: String, notes: String) -> Unit
) {
    val serviceCategories = listOf(
        "تعویض روغن موتور و فیلترها",
        "تعویض لنت ترمز",
        "تعویض تسمه تایم و دینام",
        "سرویس شمع و وایر",
        "تعویض روغن گیربکس (واسکازین)",
        "تعویض ضدیخ و رادیاتور",
        "سایر موارد نگهداری"
    )

    var selectedCategory by remember { mutableStateOf(serviceCategories[0]) }

    // Quick item checkable chips
    var changeOil by remember { mutableStateOf(true) }
    var changeOilFilter by remember { mutableStateOf(true) }
    var changeAirFilter by remember { mutableStateOf(true) }
    var changeFuelFilter by remember { mutableStateOf(false) }
    var changeCabinFilter by remember { mutableStateOf(false) }
    var changeBrakePads by remember { mutableStateOf(false) }
    var changeTimingBelt by remember { mutableStateOf(false) }
    var changeGearboxOil by remember { mutableStateOf(false) }

    var odometer by remember { mutableStateOf(vehicle.currentOdometer.toString()) }
    var nextOdometer by remember { mutableStateOf((vehicle.currentOdometer + 6000).toString()) }
    var costInput by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val builtItems = remember(changeOil, changeOilFilter, changeAirFilter, changeFuelFilter, changeCabinFilter, changeBrakePads, changeTimingBelt, changeGearboxOil) {
        val list = mutableListOf<String>()
        if (changeOil) list.add("روغن موتور")
        if (changeOilFilter) list.add("فیلتر روغن")
        if (changeAirFilter) list.add("فیلتر هوا")
        if (changeFuelFilter) list.add("فیلتر بنزین")
        if (changeCabinFilter) list.add("فیلتر اتاق")
        if (changeBrakePads) list.add("لنت ترمز")
        if (changeTimingBelt) list.add("تسمه تایم")
        if (changeGearboxOil) list.add("روغن گیربکس")
        list.joinToString(" + ")
    }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "ثبت سرویس دوره‌ای خودرو",
        subtitle = "خودرو ${vehicle.title} (${vehicle.formattedPlate})",
        icon = Icons.Default.BuildCircle,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

                // Category selector
                Column {
                    Text("نوع سرویس:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(serviceCategories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // Quick item checkboxes
                Column {
                    Text("اقلام تعویض شده در این سرویس:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(selected = changeOil, onClick = { changeOil = !changeOil }, label = { Text("روغن موتور", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = changeOilFilter, onClick = { changeOilFilter = !changeOilFilter }, label = { Text("فیلتر روغن", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = changeAirFilter, onClick = { changeAirFilter = !changeAirFilter }, label = { Text("فیلتر هوا", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(selected = changeFuelFilter, onClick = { changeFuelFilter = !changeFuelFilter }, label = { Text("فیلتر بنزین", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = changeCabinFilter, onClick = { changeCabinFilter = !changeCabinFilter }, label = { Text("فیلتر اتاق", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = changeBrakePads, onClick = { changeBrakePads = !changeBrakePads }, label = { Text("لنت ترمز", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(selected = changeTimingBelt, onClick = { changeTimingBelt = !changeTimingBelt }, label = { Text("تسمه تایم", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = changeGearboxOil, onClick = { changeGearboxOil = !changeGearboxOil }, label = { Text("روغن گیربکس", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                    }
                }

                // Odometer & Next Due Odometer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = odometer,
                        onValueChange = {
                            odometer = it
                            val current = it.toIntOrNull() ?: vehicle.currentOdometer
                            nextOdometer = (current + 6000).toString()
                        },
                        label = { Text("کیلومتر فعلی") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = nextOdometer,
                        onValueChange = { nextOdometer = it },
                        label = { Text("کیلومتر بعدی") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Cost & Mechanic
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = costInput,
                        onValueChange = { costInput = it },
                        label = { Text("هزینه (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("نام تعویض‌روغنی/تعمیرگاه") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات و برند روغن/لنت (اختیاری)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Action Buttons
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        val odo = odometer.toIntOrNull() ?: vehicle.currentOdometer
                        val nextOdo = nextOdometer.toIntOrNull() ?: (odo + 6000)
                        val cost = costInput.toLongOrNull() ?: 0L
                        val finalItems = if (builtItems.isNotBlank()) builtItems else selectedCategory
                        onConfirm(selectedCategory, finalItems, odo, nextOdo, System.currentTimeMillis(), cost, shopName, notes)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ثبت در دفترچه سرویس خودرو")
                }
        }
    }
}

// =======================================================================================
// DIALOG: ADD SERVICE REMINDER
// =======================================================================================
@Composable
private fun AddReminderDialog(
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
        "تعویض روغن گیربکس",
        "معاینه فنی سالیانه"
    )

    var selectedService by remember { mutableStateOf(presets[0]) }
    var targetOdometer by remember { mutableStateOf((vehicle.currentOdometer + 5000).toString()) }
    var notes by remember { mutableStateOf("") }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "تنظیم یادآور سرویس",
        subtitle = "خودرو ${vehicle.title} (${vehicle.formattedPlate})",
        icon = Icons.Default.NotificationsActive,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("انتخاب نوع سرویس:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    presets.take(4).forEach { p ->
                        FilterChip(
                            selected = selectedService == p,
                            onClick = { selectedService = p },
                            label = { Text(p, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = targetOdometer,
                    onValueChange = { targetOdometer = it },
                    label = { Text("کیلومتر هدف جهت انجام سرویس") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات (نوع روغن، مارک لنت، تعمیرگاه)") },
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
                        Text("فعال‌سازی هشدار")
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

// =======================================================================================
// COMPONENT: TECHNICAL INSPECTION REMINDER CARD
// =======================================================================================
@Composable
private fun TechnicalInspectionCard(
    vehicle: VehicleEntity,
    onEditClick: () -> Unit,
    onTriggerAlert: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val expiryMillis = vehicle.effectiveInspectionExpiryMillis
    val now = System.currentTimeMillis()
    val daysRemaining = ((expiryMillis - now) / (24L * 3600 * 1000)).toInt()

    val (statusTitle, statusColor, statusContainer, statusDesc) = when {
        daysRemaining < 0 -> Quadruple(
            "معاینه فنی منقضی شده است! 🔴",
            Color(0xFFEF4444),
            Color(0xFFFEE2E2),
            "معاینه فنی خودرو به پایان رسیده و مشمول جریمه روزانه ۵۲,۵۰۰ تومانی پلیس راهور می‌باشد!"
        )
        daysRemaining <= vehicle.inspectionNotifyDaysBefore -> Quadruple(
            "هشدار: نزدیک به موعد انقضا 🟡",
            Color(0xFFF59E0B),
            Color(0xFFFEF3C7),
            "تنها $daysRemaining روز تا پایان اعتبار معاینه فنی باقی مانده است. جهت اخذ نوبت اقدام فرمایید."
        )
        else -> Quadruple(
            "معاینه فنی معتبر و فعال است 🟢",
            Color(0xFF10B981),
            Color(0xFFD1FAE5),
            "وضعیت معاینه فنی خودرو معتبر می‌باشد ($daysRemaining روز مانده تا موعد تمدید)."
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("technical_inspection_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "یادآور تاریخ انقضای معاینه فنی",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${vehicle.title} (${vehicle.formattedPlate})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = statusContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (daysRemaining >= 0) "$daysRemaining روز مانده" else "منقضی شده",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Status Details Box
            Surface(
                color = statusContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = statusTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = statusDesc,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Info rows: Expiration Date & Center Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تاریخ انقضای معاینه:",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = PersianDateHelper.toPersianDate(expiryMillis),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "مرکز معاینه فنی:",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = vehicle.inspectionCenter.ifBlank { "مرکز مکانیزه بیهقی" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تمدید یا تغییر تاریخ", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onTriggerAlert,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تنظیم اعلان", fontSize = 11.sp)
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// =======================================================================================
// DIALOG: EDIT TECHNICAL INSPECTION EXPIRATION & REMINDERS
// =======================================================================================
@Composable
private fun EditInspectionDialog(
    vehicle: VehicleEntity,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, Int) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    var selectedDaysOffset by remember { mutableStateOf(365) } // Default 1 year from today
    var customCenterName by remember { mutableStateOf(vehicle.inspectionCenter.ifBlank { "مرکز مکانیزه بیهقی" }) }
    var notifyDaysBefore by remember { mutableStateOf(vehicle.inspectionNotifyDaysBefore) }

    val presetCenters = listOf(
        "مرکز معاینه فنی بیهقی",
        "مرکز معاینه فنی نیایش",
        "مرکز معاینه فنی سراج",
        "مرکز معاینه فنی آبشناسان",
        "مرکز معاینه فنی خاوران"
    )

    val presetDurations = listOf(
        365 to "۱ سال (استاندارد)",
        180 to "۶ ماه (خودرو عمومی)",
        90 to "۳ ماه (کوتاه مدت)",
        30 to "۱ ماه (موقت)"
    )

    val targetExpiryMillis = remember(selectedDaysOffset) {
        System.currentTimeMillis() + (selectedDaysOffset.toLong() * 24 * 3600 * 1000)
    }

    M3ThemedDialogContainer(
        onDismissRequest = onDismiss,
        title = "تنظیم تاریخ معاینه فنی",
        subtitle = "خودرو ${vehicle.title} (${vehicle.formattedPlate})",
        icon = Icons.Default.FactCheck,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Validity duration chips
                Text("مدت اعتبار معاینه فنی:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetDurations.take(2).forEach { (days, label) ->
                        FilterChip(
                            selected = selectedDaysOffset == days,
                            onClick = { selectedDaysOffset = days },
                            label = { Text(label, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetDurations.drop(2).forEach { (days, label) ->
                        FilterChip(
                            selected = selectedDaysOffset == days,
                            onClick = { selectedDaysOffset = days },
                            label = { Text(label, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Calculated Date Banner
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تاریخ موعد انقضا:", fontSize = 11.sp)
                        Text(
                            text = PersianDateHelper.toPersianDate(targetExpiryMillis),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Center name input
                OutlinedTextField(
                    value = customCenterName,
                    onValueChange = { customCenterName = it },
                    label = { Text("نام مرکز معاینه فنی") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Quick preset centers
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(presetCenters) { center ->
                        AssistChip(
                            onClick = { customCenterName = center },
                            label = { Text(center.replace("مرکز معاینه فنی ", ""), fontSize = 9.5.sp) }
                        )
                    }
                }

                // Reminder alert lead days
                Text("زمان ارسال اعلان یادآوری قبل از موعد:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(30 to "۳۰ روز قبل", 15 to "۱۵ روز قبل", 7 to "۷ روز قبل", 3 to "۳ روز قبل").forEach { (days, label) ->
                        FilterChip(
                            selected = notifyDaysBefore == days,
                            onClick = { notifyDaysBefore = days },
                            label = { Text(label, fontSize = 9.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onConfirm(targetExpiryMillis, customCenterName, notifyDaysBefore)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ذخیره و فعال‌سازی")
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
