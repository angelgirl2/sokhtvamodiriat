package com.angelgirlbrand.modiratsokhtandestelam

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.angelgirlbrand.modiratsokhtandestelam.data.local.AppDatabase
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.security.AppThemeColor
import com.angelgirlbrand.modiratsokhtandestelam.security.DarkModePref
import com.angelgirlbrand.modiratsokhtandestelam.security.SecurityManager
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.ArvandPlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.CustomAppNotificationBanner
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.CustomThemedConfirmDialog
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.InsuranceAgencyPickerField
import com.angelgirlbrand.modiratsokhtandestelam.util.InsuranceAgencies
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.CustomThemedExitDialog
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.DynamicStatusWaitingTracker
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.IranianMotorcyclePlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.IranianPlateLetterPicker
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.IranianPlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.SmartAdaptivePlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.VehicleFormDialog
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.VehiclePlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.AddVehicleReminderDialog
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.MainScreen
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.NavigationItem
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.SplashScreen
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.VehicleDetailScreen
import com.angelgirlbrand.modiratsokhtandestelam.ui.theme.MyApplicationTheme
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModelFactory
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.MonthlyFinanceAndFinesChartWidget
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.TripCostEstimatorWidget
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.SectionIntroGuideCard
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.OnboardingScreen
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.VehicleQrScannerScreen
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModelFactory

enum class AppDestination {
    SPLASH,
    ONBOARDING,
    MAIN,
    QR_SCANNER
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check for app update to clear previous cache cleanly
        val sp = getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)
        val lastRunVersion = sp.getString("last_run_version_name", "")
        val currentVersion = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "1.0"
        }
        if (lastRunVersion != currentVersion) {
            try {
                cacheDir.deleteRecursively()
                externalCacheDir?.deleteRecursively()
            } catch (e: Exception) {
                // Ignore silent deletion errors
            }
            sp.edit().putString("last_run_version_name", currentVersion).apply()
        }

        // Room Database Setup directly initialized in MainActivity for vehicle storage
        val appDatabase = AppDatabase.getDatabase(applicationContext)
        val app = application as FuelApplication
        val repository = app.repository
        val securityManager = app.securityManager
        val notificationHelper = app.notificationHelper

        setContent {
            val fuelViewModel: FuelViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = FuelViewModelFactory(repository, notificationHelper)
            )
            val baleViewModel: BaleServiceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = BaleServiceViewModelFactory(repository, securityManager)
            )

            var currentDestination by remember { mutableStateOf(AppDestination.SPLASH) }
            var vehiclePrefillFromScan by remember { mutableStateOf<VehicleEntity?>(null) }
            var currentThemeColor by remember { mutableStateOf(securityManager.getSelectedThemeColor()) }
            var currentDarkModePref by remember { mutableStateOf(securityManager.getDarkModePref()) }
            var activeTab by remember { mutableStateOf<NavigationItem?>(null) }
            var viewingVehicleDetailId by remember { mutableStateOf<Long?>(null) }

            // Observe room data for automatic Railway cloud sync
            val vehicles by fuelViewModel.vehicles.collectAsState()
            val fuelLogs by fuelViewModel.fuelLogs.collectAsState()
            val reminders by fuelViewModel.reminders.collectAsState()

            // Automatic background sync with Railway whenever database items change or app opens
            LaunchedEffect(vehicles, fuelLogs, reminders) {
                if (vehicles.isNotEmpty()) {
                    baleViewModel.syncWithRailway(vehicles)
                }
            }

            val submissionMsg by baleViewModel.submissionMessage.collectAsState()
            var activeBannerMessage by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(submissionMsg) {
                if (!submissionMsg.isNullOrBlank()) {
                    activeBannerMessage = submissionMsg
                }
            }

            LaunchedEffect(Unit) {
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.messageFlow.collect { msg ->
                    if (msg.isNotBlank()) {
                        activeBannerMessage = msg
                    }
                }
            }

            MyApplicationTheme(
                appThemeColor = currentThemeColor,
                darkModePref = currentDarkModePref
            ) {
                // Mandatory localized RTL support for all application screens
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                    AnimatedContent(
                        targetState = currentDestination,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(450, easing = FastOutSlowInEasing)) +
                                scaleIn(initialScale = 0.95f, animationSpec = tween(450, easing = FastOutSlowInEasing)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                        scaleOut(targetScale = 1.05f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                )
                        },
                        label = "destinationTransition"
                    ) { destination ->
                        when (destination) {
                            AppDestination.SPLASH -> {
                                SplashScreen(
                                    securityManager = securityManager,
                                    onNavigateToNext = {
                                        if (!securityManager.isOnboardingSeen()) {
                                            currentDestination = AppDestination.ONBOARDING
                                        } else {
                                            currentDestination = AppDestination.MAIN
                                        }
                                    }
                                )
                            }

                            AppDestination.ONBOARDING -> {
                                OnboardingScreen(
                                    securityManager = securityManager,
                                    onFinish = {
                                        currentDestination = AppDestination.MAIN
                                    }
                                )
                            }

                            AppDestination.MAIN -> {
                                if (viewingVehicleDetailId != null) {
                                    // Vehicle Details Screen
                                    VehicleDetailScreen(
                                        vehicleId = viewingVehicleDetailId!!,
                                        fuelViewModel = fuelViewModel,
                                        onNavigateBack = { viewingVehicleDetailId = null }
                                    )
                                } else if (activeTab == null) {
                                    ModernDashboardLayout(
                                        fuelViewModel = fuelViewModel,
                                        baleViewModel = baleViewModel,
                                        securityManager = securityManager,
                                        onNavigateToFuel = { activeTab = NavigationItem.FUEL },
                                        onNavigateToFines = { activeTab = NavigationItem.INQUIRY },
                                        onNavigateToRequests = { activeTab = NavigationItem.SERVICES },
                                        onNavigateToReminders = { activeTab = NavigationItem.REMINDERS },
                                        onNavigateToSettings = { activeTab = NavigationItem.SETTINGS },
                                        onNavigateToVehicleDetail = { vehicleId -> viewingVehicleDetailId = vehicleId },
                                        onNavigateToQrScanner = { currentDestination = AppDestination.QR_SCANNER },
                                        vehiclePrefillFromScan = vehiclePrefillFromScan,
                                        onClearVehiclePrefill = { vehiclePrefillFromScan = null }
                                    )
                                } else {
                                    MainScreen(
                                        fuelViewModel = fuelViewModel,
                                        baleViewModel = baleViewModel,
                                        securityManager = securityManager,
                                        initialTab = activeTab ?: NavigationItem.FUEL,
                                        onNavigateBackToDashboard = { activeTab = null },
                                        currentThemeColor = currentThemeColor,
                                        currentDarkModePref = currentDarkModePref,
                                        onThemeColorChanged = { newColor ->
                                            securityManager.setSelectedThemeColor(newColor)
                                            currentThemeColor = newColor
                                        },
                                        onDarkModePrefChanged = { newMode ->
                                            securityManager.setDarkModePref(newMode)
                                            currentDarkModePref = newMode
                                        }
                                    )
                                }
                            }

                            AppDestination.QR_SCANNER -> {
                                VehicleQrScannerScreen(
                                    onNavigateBack = {
                                        currentDestination = AppDestination.MAIN
                                    },
                                    onVehicleScannedAndConfirmed = { vehicleEntity ->
                                        fuelViewModel.insertVehicle(vehicleEntity)
                                        currentDestination = AppDestination.MAIN
                                    },
                                    onEditManually = { scannedInfo ->
                                        vehiclePrefillFromScan = scannedInfo.toVehicleEntity()
                                        currentDestination = AppDestination.MAIN
                                    }
                                )
                            }
                        }
                    }
                }

                CustomAppNotificationBanner(
                    message = activeBannerMessage,
                    onDismiss = {
                        activeBannerMessage = null
                        baleViewModel.clearMessage()
                    },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
}

// =============================================================================
// MODERN COMPOSE DASHBOARD LAYOUT (LOCALIZED RTL WITH AUTO SYNC & SERVICE REMINDERS)
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDashboardLayout(
    fuelViewModel: FuelViewModel,
    baleViewModel: BaleServiceViewModel,
    securityManager: SecurityManager,
    onNavigateToFuel: () -> Unit,
    onNavigateToFines: () -> Unit,
    onNavigateToRequests: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToVehicleDetail: (Long) -> Unit,
    onNavigateToQrScanner: () -> Unit = {},
    vehiclePrefillFromScan: VehicleEntity? = null,
    onClearVehiclePrefill: () -> Unit = {}
) {
    val context = LocalContext.current
    val vehicles by fuelViewModel.vehicles.collectAsState()
    val fuelLogs by fuelViewModel.fuelLogs.collectAsState()
    val reminders by fuelViewModel.reminders.collectAsState()
    val inquiries by baleViewModel.inquiries.collectAsState()
    val serviceRequests by baleViewModel.serviceRequests.collectAsState()
    val isSyncing by baleViewModel.isSubmitting.collectAsState()
    val syncStatusMsg by baleViewModel.syncStatus.collectAsState()

    val selectedVehicleId by fuelViewModel.selectedVehicleId.collectAsState()
    val activeVehicle: VehicleEntity? = vehicles.firstOrNull { it.id == selectedVehicleId } ?: vehicles.firstOrNull()

    var showExitDialog by remember { mutableStateOf(false) }
    var showVehicleFormDialog by remember { mutableStateOf(false) }
    var showAddVehicleBottomSheet by remember { mutableStateOf(false) }
    var showQuickAddReminderDialog by remember { mutableStateOf(false) }
    var showDirectRequestDialog by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<VehicleEntity?>(null) }
    var vehicleToDelete by remember { mutableStateOf<VehicleEntity?>(null) }

    LaunchedEffect(vehiclePrefillFromScan) {
        if (vehiclePrefillFromScan != null) {
            showAddVehicleBottomSheet = true
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Active vehicle service & maintenance reminders
    val vehicleReminders = remember(reminders, activeVehicle?.id) {
        if (activeVehicle != null) reminders.filter { it.vehicleId == activeVehicle.id } else emptyList()
    }
    val activeOilReminder = vehicleReminders.firstOrNull { it.serviceType.contains("روغن") && !it.isCompleted }

    // Insurance calculation
    val currentTime = System.currentTimeMillis()
    val insuranceDaysRemaining = remember(activeVehicle?.effectiveInsuranceExpiryMillis, currentTime) {
        if (activeVehicle != null) {
            ((activeVehicle.effectiveInsuranceExpiryMillis - currentTime) / (24 * 3600 * 1000L)).toInt()
        } else 0
    }

    // Android back handler on dashboard prompts exit confirmation
    BackHandler {
        showExitDialog = true
    }

    // ModalBottomSheet for Adding Cars and Motorcycles to Room Database
    if (showAddVehicleBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddVehicleBottomSheet = false
                onClearVehiclePrefill()
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AddVehicleBottomSheet(
                initialVehicle = vehiclePrefillFromScan,
                onDismiss = {
                    showAddVehicleBottomSheet = false
                    onClearVehiclePrefill()
                },
                onScanQr = {
                    showAddVehicleBottomSheet = false
                    onNavigateToQrScanner()
                },
                onConfirm = { title, f2, letter, l3, city, fuelType, cap, odo, vType ->
                    fuelViewModel.addVehicle(title, f2, letter, l3, city, fuelType, cap, odo, vType)
                    showAddVehicleBottomSheet = false
                    onClearVehiclePrefill()
                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("$vType «$title» با موفقیت ثبت شد")
                }
            )
        }
    }

    // Dialog 1: Edit Vehicle (Cars & Motorcycles)
    if (showVehicleFormDialog) {
        VehicleFormDialog(
            vehicleToEdit = vehicleToEdit,
            onDismiss = {
                showVehicleFormDialog = false
                vehicleToEdit = null
            },
            onConfirm = { title, f2, letter, l3, city, fuelType, cap, odo, vType ->
                if (vehicleToEdit != null) {
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
                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("اطلاعات $vType با موفقیت بروزرسانی شد")
                } else {
                    fuelViewModel.addVehicle(title, f2, letter, l3, city, fuelType, cap, odo, vType)
                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("$vType جدید با موفقیت ثبت شد")
                }
                showVehicleFormDialog = false
                vehicleToEdit = null
            }
        )
    }

    // Dialog 2: Delete Vehicle Confirmation (Custom Themed Dialog)
    if (vehicleToDelete != null) {
        CustomThemedConfirmDialog(
            onDismissRequest = { vehicleToDelete = null },
            title = "حذف وسیله نقلیه",
            message = "آیا از حذف «${vehicleToDelete!!.title}» از برنامه اطمینان دارید؟ کلیه سوابق مصرف سوخت و استعلام‌های مرتبط نیز پاک خواهند شد.",
            confirmText = "حذف قطعی",
            dismissText = "انصراف",
            isDestructive = true,
            icon = Icons.Default.DeleteOutline,
            onConfirm = {
                fuelViewModel.deleteVehicle(vehicleToDelete!!)
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وسیله نقلیه با موفقیت حذف شد")
                vehicleToDelete = null
            }
        )
    }

    // Dialog 3: Quick Add Reminder (Oil change / Service) from Dashboard
    if (showQuickAddReminderDialog && activeVehicle != null) {
        AddVehicleReminderDialog(
            vehicle = activeVehicle,
            onDismiss = { showQuickAddReminderDialog = false },
            onConfirm = { serviceType, targetDate, targetOdo, notes ->
                fuelViewModel.addServiceReminder(
                    vehicleId = activeVehicle.id,
                    serviceType = serviceType,
                    targetDateMillis = targetDate,
                    targetOdometer = targetOdo,
                    notes = notes
                )
                showQuickAddReminderDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("یادآور $serviceType برای ${activeVehicle.title} تنظیم شد")
            }
        )
    }

    // Dialog 4: Direct Bale Service Request Form
    if (showDirectRequestDialog) {
        DirectBaleServiceRequestDialog(
            vehicles = vehicles,
            isSubmitting = isSyncing,
            onDismiss = { showDirectRequestDialog = false },
            onSubmit = { type, title, name, nationalCode, phone, plate, notes ->
                baleViewModel.submitServiceRequest(
                    requestType = type,
                    title = title,
                    fullName = name,
                    nationalCode = nationalCode,
                    phoneNumber = phone,
                    vehiclePlate = plate,
                    address = notes
                )
                showDirectRequestDialog = false
                com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("درخواست شما با موفقیت برای ادمین ارسال شد")
            }
        )
    }

    // Dialog 5: Exit App (Custom Themed Dialog)
    if (showExitDialog) {
        CustomThemedExitDialog(
            onDismissRequest = { showExitDialog = false },
            onConfirmExit = {
                showExitDialog = false
                (context as? Activity)?.finish()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (activeVehicle?.isMotorcycle == true) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "سامانه مدیریت خودرو و موتور",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = activeVehicle?.title ?: "مدیریت سوخت و استعلام",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "تنظیمات",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.shadow(2.dp)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddVehicleBottomSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("افزودن وسیله", fontWeight = FontWeight.Bold, fontSize = 13.5.sp) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .testTag("add_vehicle_fab")
                    .padding(bottom = 8.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // One-time interactive educational guide card for Dashboard & Fuel Card
            SectionIntroGuideCard(
                sectionKey = "dashboard_overview",
                title = "راهنمای داشبورد و کارت هوشمند سوخت",
                description = "به سامانه مدیریت خودرو و سوخت خوش آمدید. در این داشبورد می‌توانید وضعیت سهمیه و کارت سوخت خود را پایش کرده و به سرعت به سایر بخش‌ها دسترسی داشته باشید.",
                tips = listOf(
                    "برای مشاهده پشت کارت سوخت و اطلاعات VIN و سریال، روی کارت ضربه بزنید.",
                    "برای افزودن وسیله نقلیه جدید از دکمه شناور پایین صفحه استفاده کنید.",
                    "با لمس کارت وسیله نقلیه می‌توانید به جزئیات مصرف و تاریخچه دسترسی پیدا کنید."
                ),
                icon = Icons.Default.CreditCard,
                accentColor = Color(0xFF0284C7),
                securityManager = securityManager
            )

            // 0.5. CAMERA QR CODE SCANNER QUICK ACTION CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToQrScanner() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.15f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "اسکن بارکد و QR کد کارت خودرو",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "استخراج آنی مشخصات پلاک و مدل با دوربین و ML Kit",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onNavigateToQrScanner,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اسکن", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // -----------------------------------------------------------------
            // 1. VEHICLES (CAR / MOTORCYCLE) SWIPEABLE OVERVIEW (کشیدن برای سایر پلاک‌ها)
            // -----------------------------------------------------------------
            if (vehicles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("خودروها و پلاک‌های ثبت‌شده (${vehicles.size})", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                        if (vehicles.size > 1) {
                            Text("◄ برای دیدن سایر پلاک‌ها بکشید", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        } else {
                            TextButton(
                                onClick = { showAddVehicleBottomSheet = true },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("افزودن پلاک جدید", fontSize = 11.sp)
                            }
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(vehicles) { veh ->
                            val isSelected = activeVehicle?.id == veh.id
                            Card(
                                modifier = Modifier
                                    .width(300.dp)
                                    .clickable {
                                        fuelViewModel.selectVehicle(veh.id)
                                        onNavigateToVehicleDetail(veh.id)
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Top row: Title, badge, and Edit/Delete buttons
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
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (veh.isMotorcycle) Color(0xFF8B5CF6).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.padding(2.dp)
                                            ) {
                                                Text(
                                                    text = if (veh.isMotorcycle) "🏍️ موتور" else "🚗 خودرو",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (veh.isMotorcycle) Color(0xFF7C3AED) else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = veh.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${String.format("%,d", veh.currentOdometer)} کیلومتر",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    vehicleToEdit = veh
                                                    showVehicleFormDialog = true
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "ویرایش",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    vehicleToDelete = veh
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.DeleteOutline,
                                                    contentDescription = "حذف",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Complete authentic Iranian License Plate View
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        VehiclePlateView(vehicle = veh)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = {
                                                fuelViewModel.selectVehicle(veh.id)
                                                onNavigateToVehicleDetail(veh.id)
                                            },
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Text("جزئیات و سوابق ←", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        if (isSelected) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    "انتخاب شده",
                                                    fontSize = 9.5.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Empty vehicle state with quick Add button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "وسیله نقلیه‌ای ثبت نشده است",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "برای مدیریت مصرف سوخت، یادآور تعویض روغن و بیمه، خودرو یا موتور خود را ثبت کنید.",
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                showAddVehicleBottomSheet = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("افزودن خودرو یا موتور")
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // BALE BOT DIRECT IN-APP REQUEST CARD & FORM (استعلام و درخواست مستقیم بدون خروج)
            // -----------------------------------------------------------------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bale_bot_request_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f))
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
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Assignment,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Column {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF38BDF8).copy(alpha = 0.3f)
                                    ) {
                                        Text(
                                            text = "سامانه اختصاصی کارشناس و مدیر",
                                            color = Color(0xFFE0F2FE),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "ثبت و ارسال مستقیم درخواست به کارشناس و مدیر",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Text(
                            text = "تکمیل فرم درون‌برنامه‌ای جهت استعلام رسمی خلافی، عوارض، صدور بیمه‌نامه و کارت سوخت و ارسال مستقیم بدون خروج از برنامه",
                            fontSize = 11.5.sp,
                            color = Color(0xFFBAE6FD),
                            lineHeight = 18.sp
                        )

                        Button(
                            onClick = {
                                showDirectRequestDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("open_bale_form_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF0369A1)
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تکمیل فرم و ارسال مستقیم درخواست ✍️",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // 2. SECTION HEADER: QUICK ACCESS SERVICES
            // -----------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "دسترسی سریع به خدمات",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "۴ بخش اصلی",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // -----------------------------------------------------------------
            // 3. THE 4 QUICK-ACCESS CARDS (2x2 GRID)
            // -----------------------------------------------------------------
            // ROW 1: FUEL TRACKING & TRAFFIC FINES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Fuel Tracking
                ModernQuickAccessCard(
                    title = "مدیریت سوخت",
                    subtitle = "Fuel Tracking",
                    stat = "${fuelLogs.size} باک ثبت‌شده",
                    description = "ثبت سوخت‌گیری خودرو و موتور، مصرف ۱۰۰ کیلومتر و هزینه",
                    icon = Icons.Default.LocalGasStation,
                    gradient = Brush.linearGradient(
                        listOf(Color(0xFF047857), Color(0xFF10B981))
                    ),
                    buttonText = "ورود به بخش سوخت",
                    onClick = onNavigateToFuel,
                    modifier = Modifier.weight(1f)
                )

                // Card 2: Traffic Fines
                ModernQuickAccessCard(
                    title = "استعلام خلافی",
                    subtitle = "Traffic Fines",
                    stat = "${inquiries.size} استعلام",
                    description = "استعلام و تسویه رسمی خلافی، عوارض آزادراهی و سالیانه",
                    icon = Icons.Default.ReceiptLong,
                    gradient = Brush.linearGradient(
                        listOf(Color(0xFFBE123C), Color(0xFFF43F5E))
                    ),
                    buttonText = "استعلام خلافی",
                    onClick = onNavigateToFines,
                    modifier = Modifier.weight(1f)
                )
            }

            // ROW 2: REQUESTS (INSURANCE, FUEL CARD) & SERVICE REMINDERS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 3: Requests & Insurance (انتقال بیمه به ثبت درخواست)
                ModernQuickAccessCard(
                    title = "ثبت درخواست‌ها",
                    subtitle = "Services & Insurance",
                    stat = "بیمه و کارت سوخت",
                    description = "صدور آنلاین بیمه شخص ثالث و بدنه، کارت سوخت و پیگیری",
                    icon = Icons.Default.Assignment,
                    gradient = Brush.linearGradient(
                        listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6))
                    ),
                    buttonText = "ثبت و پیگیری درخواست",
                    onClick = onNavigateToRequests,
                    modifier = Modifier.weight(1f)
                )

                // Card 4: Service Reminders
                val reminderSubtitle = if (activeOilReminder != null) "روغن: %,d km".format(activeOilReminder.targetOdometer)
                else "${vehicleReminders.size} یادآور فعال"

                ModernQuickAccessCard(
                    title = "سرویس و نگهداری",
                    subtitle = "Service Reminders",
                    stat = reminderSubtitle,
                    description = "تعویض روغن، لنت، فیلترها، تسمه‌تایم و هشدارهای دوره‌ای",
                    icon = Icons.Default.Build,
                    gradient = Brush.linearGradient(
                        listOf(Color(0xFF6D28D9), Color(0xFF8B5CF6))
                    ),
                    buttonText = "بررسی سرویس‌ها",
                    onClick = onNavigateToReminders,
                    modifier = Modifier.weight(1f)
                )
            }

            // -----------------------------------------------------------------
            // 4. SMART TRIP COST ESTIMATOR WIDGET (پیش‌بینی هوشمند هزینه و بنزین سفر)
            // -----------------------------------------------------------------
            TripCostEstimatorWidget(
                activeVehicle = activeVehicle,
                fuelLogs = fuelLogs
            )

            // -----------------------------------------------------------------
            // 5. MONTHLY FINANCE & FINES RECHARTS-INSPIRED COLUMN CHART WIDGET
            // -----------------------------------------------------------------
            MonthlyFinanceAndFinesChartWidget(
                fuelLogs = fuelLogs,
                inquiries = inquiries
            )

            // -----------------------------------------------------------------
            // 6. TIDY PERFORMANCE & METRICS STRIP
            // -----------------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricMiniItem(
                        icon = Icons.Default.LocalGasStation,
                        label = "سوخت‌گیری‌ها",
                        value = "${fuelLogs.size} بار",
                        color = Color(0xFF10B981)
                    )
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    MetricMiniItem(
                        icon = Icons.Default.Search,
                        label = "استعلام‌ها",
                        value = "${inquiries.size} مورد",
                        color = Color(0xFFF43F5E)
                    )
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    MetricMiniItem(
                        icon = Icons.Default.Assignment,
                        label = "درخواست‌ها",
                        value = "${serviceRequests.size} مورد",
                        color = Color(0xFF3B82F6)
                    )
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    MetricMiniItem(
                        icon = Icons.Default.Build,
                        label = "یادآورها",
                        value = "${reminders.size} فعال",
                        color = Color(0xFF8B5CF6)
                    )
                }
            }

            // Bottom spacing so FAB does not obscure the last metrics card
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

// =============================================================================
// MODAL BOTTOM SHEET: ADD VEHICLE (CAR / MOTORCYCLE) TO ROOM DATABASE
// =============================================================================

@Composable
fun AddVehicleBottomSheet(
    initialVehicle: VehicleEntity? = null,
    onDismiss: () -> Unit,
    onScanQr: () -> Unit = {},
    onConfirm: (
        title: String,
        plateF2: String,
        plateLetter: String,
        plateL3: String,
        plateCity: String,
        fuelType: String,
        tankCapacity: Double,
        odometer: Int,
        vehicleType: String
    ) -> Unit
) {
    var vehicleType by remember(initialVehicle) { mutableStateOf(initialVehicle?.vehicleType ?: VehicleEntity.TYPE_CAR) }
    var title by remember(initialVehicle) { mutableStateOf(initialVehicle?.title.orEmpty()) }
    var plateF2 by remember(initialVehicle) { mutableStateOf(initialVehicle?.plateFirst2.orEmpty()) }
    var plateLetter by remember(initialVehicle) { mutableStateOf(initialVehicle?.plateLetter?.ifEmpty { "ب" } ?: "ب") }
    var plateL3 by remember(initialVehicle) { mutableStateOf(initialVehicle?.plateLast3.orEmpty()) }
    var plateCity by remember(initialVehicle) { mutableStateOf(initialVehicle?.plateCityCode?.ifEmpty { "11" } ?: "11") }
    var arvandDigits by remember(initialVehicle) {
        mutableStateOf(
            if (initialVehicle?.isArvand == true) initialVehicle.plateLast3.ifEmpty { initialVehicle.plateFirst2 } else "12365"
        )
    }
    var fuelType by remember(initialVehicle) { mutableStateOf(initialVehicle?.fuelType?.ifEmpty { "بنزین معمولی" } ?: "بنزین معمولی") }
    var tankCapacity by remember(initialVehicle) {
        mutableStateOf(
            initialVehicle?.tankCapacity?.toInt()?.toString()
                ?: if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) "10" else "50"
        )
    }
    var currentOdometer by remember(initialVehicle) { mutableStateOf((initialVehicle?.currentOdometer ?: 0).toString()) }

    val fuelTypes = if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) {
        listOf("بنزین معمولی", "بنزین سوپر")
    } else {
        listOf("بنزین معمولی", "بنزین سوپر", "دوگانه‌سوز CNG", "گازوئیل")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(13.dp)
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
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = if (initialVehicle != null) "تکمیل مشخصات استخراج‌شده از QR کد" else "افزودن وسیله نقلیه جدید",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (initialVehicle != null) "اطلاعات از بارکد کارت خودرو بازخوانی شد" else "ثبت اطلاعات مدل، پلاک (ملی یا اروندی) و ظرفیت باک",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "بستن", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Camera QR Scanner Shortcut Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0284C7).copy(alpha = 0.15f),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onScanQr() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "اسکن بارکد یا QR کد کارت خودرو با دوربین",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "تکمیل خودکار پلاک، مدل و سهمیه با بارکدخوان",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 1. انتخاب نوع وسیله (خودرو سواری، پلاک اروندی، موتورسیکلت)
        Text(
            text = "نوع وسیله نقلیه و فرمت پلاک:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = vehicleType == VehicleEntity.TYPE_CAR,
                onClick = {
                    vehicleType = VehicleEntity.TYPE_CAR
                    if (tankCapacity == "10") tankCapacity = "50"
                },
                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(15.dp)) },
                label = { Text("خودرو ملی", fontSize = 11.sp, fontWeight = if (vehicleType == VehicleEntity.TYPE_CAR) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = vehicleType == VehicleEntity.TYPE_ARVAND,
                onClick = {
                    vehicleType = VehicleEntity.TYPE_ARVAND
                    if (tankCapacity == "10") tankCapacity = "50"
                },
                label = { Text("پلاک اروندی 🌴", fontSize = 11.sp, fontWeight = if (vehicleType == VehicleEntity.TYPE_ARVAND) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.weight(1.1f),
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = vehicleType == VehicleEntity.TYPE_MOTORCYCLE,
                onClick = {
                    vehicleType = VehicleEntity.TYPE_MOTORCYCLE
                    if (tankCapacity == "50") tankCapacity = "10"
                },
                leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(15.dp)) },
                label = { Text("موتورسیکلت", fontSize = 11.sp, fontWeight = if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 2. فیلد مدل وسیله نقلیه
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = {
                Text(
                    when (vehicleType) {
                        VehicleEntity.TYPE_ARVAND -> "مدل خودرو اروندی (مثلاً: اسپورتیج، اپتیما، کامارو)"
                        VehicleEntity.TYPE_MOTORCYCLE -> "مدل موتورسیکلت (مثلاً: هوندا ۱۲۵، بنلی، وسپا)"
                        else -> "مدل خودرو (مثلاً: پژو ۲۰۶، دنا پلاس، سمند، تارا)"
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(
                    if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                    contentDescription = null
                )
            }
        )

        // 3. پیش‌نمایش گرافیکی و فیلدهای شماره پلاک با تغییر زنده رنگ
        Text(
            text = when (vehicleType) {
                VehicleEntity.TYPE_ARVAND -> "پیش‌نمایش پلاک منطقه آزاد اروند (۲ ردیف فارسی و لاتین):"
                VehicleEntity.TYPE_MOTORCYCLE -> "پیش‌نمایش پلاک ملی موتورسیکلت:"
                else -> "پیش‌نمایش زنده پلاک ملی خودرو (تغییر خودکار رنگ با نوع پلاک):"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Live graphical preview of license plate
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            SmartAdaptivePlateView(
                first2 = plateF2,
                letter = if (vehicleType == VehicleEntity.TYPE_ARVAND) "اروند" else if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) "موتور" else plateLetter,
                last3 = plateL3,
                cityCode = plateCity,
                isArvand = vehicleType == VehicleEntity.TYPE_ARVAND,
                arvandNumber = arvandDigits,
                isMotorcycle = vehicleType == VehicleEntity.TYPE_MOTORCYCLE,
                motoTop3 = plateF2,
                motoBottom5 = plateL3
            )
        }

        when (vehicleType) {
            VehicleEntity.TYPE_ARVAND -> {
                // Arvand Plate 5-digit number field
                OutlinedTextField(
                    value = arvandDigits,
                    onValueChange = { if (it.length <= 5) arvandDigits = it },
                    label = { Text("شماره ۵ رقمی پلاک اروند (مثلاً ۱۲۳۶۵)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        Text("ارقام به صورت خودکار به فارسی در ردیف بالا و به انگلیسی در ردیف پایین درج می‌گردند.")
                    }
                )
            }
            VehicleEntity.TYPE_MOTORCYCLE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = plateF2,
                        onValueChange = { if (it.length <= 3) plateF2 = it },
                        label = { Text("۳ رقم بالا (۱۲۳)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = plateL3,
                        onValueChange = { if (it.length <= 5) plateL3 = it },
                        label = { Text("۵ رقم پایین (۴۵۶۷۸)") },
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

        // 4. سایر فیلدها: ظرفیت باک و کیلومتر کارکرد
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = tankCapacity,
                onValueChange = { tankCapacity = it },
                label = { Text("ظرفیت باک (لیتر)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = currentOdometer,
                onValueChange = { currentOdometer = it },
                label = { Text("کیلومتر کارکرد") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // نوع سوخت
        Text("نوع سوخت:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            fuelTypes.forEach { ft ->
                FilterChip(
                    selected = fuelType == ft,
                    onClick = { fuelType = ft },
                    label = { Text(ft, fontSize = 10.5.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. دکمه ذخیره و ثبت وسیله نقلیه
        Spacer(modifier = Modifier.height(2.dp))
        Button(
            onClick = {
                val finalTitle = title.ifBlank {
                    when (vehicleType) {
                        VehicleEntity.TYPE_ARVAND -> "خودرو اروندی"
                        VehicleEntity.TYPE_MOTORCYCLE -> "موتورسیکلت"
                        else -> "خودرو شخصی"
                    }
                }
                val finalF2 = if (vehicleType == VehicleEntity.TYPE_ARVAND) arvandDigits.ifBlank { "12365" } else plateF2.ifBlank { "12" }
                val finalL3 = if (vehicleType == VehicleEntity.TYPE_ARVAND) arvandDigits.ifBlank { "12365" } else plateL3.ifBlank { "345" }
                val finalCity = if (vehicleType == VehicleEntity.TYPE_ARVAND) "اروند" else plateCity.ifBlank { "11" }
                val finalLetter = if (vehicleType == VehicleEntity.TYPE_ARVAND) "اروند" else if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) "موتور" else plateLetter.ifBlank { "ب" }

                onConfirm(
                    finalTitle.trim(),
                    finalF2.trim(),
                    finalLetter.trim(),
                    finalL3.trim(),
                    finalCity.trim(),
                    fuelType,
                    tankCapacity.toDoubleOrNull() ?: (if (vehicleType == VehicleEntity.TYPE_MOTORCYCLE) 10.0 else 50.0),
                    currentOdometer.toIntOrNull() ?: 0,
                    vehicleType
                )
            },
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("ذخیره و ثبت وسیله نقلیه", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

// =============================================================================
// MODERN QUICK-ACCESS CARD COMPOSABLE (M3 TIDY STYLING)
// =============================================================================

@Composable
fun ModernQuickAccessCard(
    title: String,
    subtitle: String,
    stat: String,
    description: String,
    icon: ImageVector,
    gradient: Brush,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
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
                .padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Gradient Icon & Stat Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = stat,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                    )
                }
            }

            // Titles
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Description
            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Bottom Action Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun MetricMiniItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 10.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =============================================================================
// MODAL DIALOG: DIRECT BALE SERVICE REQUEST & INQUIRY FORM (BEAUTIFUL & MODERN)
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectBaleServiceRequestDialog(
    vehicles: List<VehicleEntity>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        requestType: String,
        title: String,
        fullName: String,
        nationalCode: String,
        phoneNumber: String,
        vehiclePlate: String,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current
    val serviceTypes = listOf(
        "استعلام و تسویه رسمی خلافی خودرو و موتورسیکلت",
        "استعلام و تسویه عوارض آزادراهی",
        "استعلام عوارض سالیانه شهرداری",
        "ثبت درخواست خرید و تمدید انواع بیمه‌نامه",
        "درخواست صدور کارت هوشمند سوخت"
    )

    var selectedType by remember { mutableStateOf(serviceTypes[0]) }
    var selectedAgency by remember { mutableStateOf(InsuranceAgencies.DEFAULT_AGENCY) }
    var selectedVehicle by remember { mutableStateOf(vehicles.firstOrNull()) }
    var vehiclePlate by remember { mutableStateOf(vehicles.firstOrNull()?.formattedPlate ?: "") }
    var vehicleTitle by remember { mutableStateOf(vehicles.firstOrNull()?.title ?: "خودرو / موتور") }
    var fullName by remember { mutableStateOf("") }
    var nationalCode by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expandedTypeMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Gorgeous Gradient Header
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
                        .padding(horizontal = 18.dp, vertical = 16.dp)
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
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "ثبت و ارسال مستقیم درخواست",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ارسال آنی برای ادمین و صدور رسید پیگیری",
                                    fontSize = 11.sp,
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
                            Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (isSubmitting) {
                        DynamicStatusWaitingTracker(
                            isProcessing = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 1. Service Type Dropdown Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "نوع درخواست یا استعلام:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { expandedTypeMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = selectedType,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = expandedTypeMenu,
                                onDismissRequest = { expandedTypeMenu = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                serviceTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = type,
                                                fontSize = 12.sp,
                                                fontWeight = if (type == selectedType) FontWeight.Bold else FontWeight.Normal,
                                                color = if (type == selectedType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Assignment,
                                                contentDescription = null,
                                                tint = if (type == selectedType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            selectedType = type
                                            expandedTypeMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 1.5 Insurance Agency Selection (if insurance requested)
                    if (selectedType.contains("بیمه")) {
                        InsuranceAgencyPickerField(
                            selectedAgency = selectedAgency,
                            onAgencySelected = { selectedAgency = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = "انتخاب نمایندگی یا شرکت بیمه‌گر:"
                        )
                    }

                    // 2. Select from registered vehicles if available
                    if (vehicles.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "انتخاب از وسایل نقلیه من:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(vehicles) { v ->
                                    FilterChip(
                                        selected = selectedVehicle?.id == v.id,
                                        onClick = {
                                            selectedVehicle = v
                                            vehiclePlate = v.formattedPlate
                                            vehicleTitle = v.title
                                        },
                                        label = { Text("${v.title} (${v.formattedPlate})", fontSize = 11.sp) },
                                        leadingIcon = {
                                            Icon(
                                                if (v.isMotorcycle) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Plate Number & Title with Live Authentic Plate Preview
                    val currentVeh = selectedVehicle
                    if (currentVeh != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            VehiclePlateView(vehicle = currentVeh)
                        }
                    }

                    OutlinedTextField(
                        value = vehiclePlate,
                        onValueChange = { vehiclePlate = it },
                        label = { Text("شماره پلاک وسیله نقلیه (ملی یا اروندی)") },
                        placeholder = { Text("مثال: ۱۲ ب ۳۴۵ ایران ۱۱ یا اروند ۱۲۳۶۵") },
                        leadingIcon = {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 4. Applicant Info (Name & Phone)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("نام متقاضی") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("شماره موبایل") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = nationalCode,
                        onValueChange = { if (it.length <= 10) nationalCode = it },
                        label = { Text("کد ملی متقاضی (اجباری - ۱۰ رقم) *") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 5. Notes / Description (Mandatory)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("توضیحات تکمیلی درخواست (اجباری) *") },
                        placeholder = { Text("توضیحات مربوط به بیمه، خلافی، نوع خدمات یا سال ساخت...") },
                        leadingIcon = {
                            Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 6. Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (vehiclePlate.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("لطفاً شماره پلاک را وارد کنید")
                                    return@Button
                                }
                                if (nationalCode.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وارد کردن کد ملی الزامی است")
                                    return@Button
                                }
                                if (notes.isBlank()) {
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("وارد کردن توضیحات تکمیلی الزامی است")
                                    return@Button
                                }
                                val finalType = if (selectedType.contains("بیمه")) "$selectedType ($selectedAgency)" else selectedType
                                onSubmit(
                                    finalType,
                                    vehicleTitle,
                                    fullName.trim(),
                                    nationalCode.trim(),
                                    phoneNumber.trim(),
                                    vehiclePlate.trim(),
                                    notes.trim()
                                )
                            },
                            enabled = !isSubmitting && vehiclePlate.isNotBlank() && nationalCode.isNotBlank() && notes.isNotBlank(),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("submit_request_for_admin_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("در حال ارسال...", fontSize = 11.5.sp)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ارسال برای ادمین 🚀", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(0.85f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("انصراف", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
}
}
