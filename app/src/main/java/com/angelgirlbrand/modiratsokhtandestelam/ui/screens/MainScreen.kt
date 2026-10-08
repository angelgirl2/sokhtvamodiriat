package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.security.AppThemeColor
import com.angelgirlbrand.modiratsokhtandestelam.security.DarkModePref
import com.angelgirlbrand.modiratsokhtandestelam.security.SecurityManager
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.SectionIntroGuideCard
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel

enum class NavigationItem(
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("اطلاعیه‌ها", Icons.Default.NotificationsActive),
    FUEL("سوخت", Icons.Default.LocalGasStation),
    SERVICES("خدمات", Icons.Default.Assignment),
    TARIFFS("تعرفه و هزینه", Icons.Default.Payments),
    INQUIRY("استعلام", Icons.Default.Search),
    REMINDERS("سرویس", Icons.Default.Build),
    SETTINGS("تنظیمات", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    fuelViewModel: FuelViewModel,
    baleViewModel: BaleServiceViewModel,
    securityManager: SecurityManager,
    initialTab: NavigationItem = NavigationItem.FUEL,
    onNavigateBackToDashboard: (() -> Unit)? = null,
    currentThemeColor: AppThemeColor = AppThemeColor.SKY_BLUE,
    currentDarkModePref: DarkModePref = DarkModePref.SYSTEM,
    onThemeColorChanged: (AppThemeColor) -> Unit = {},
    onDarkModePrefChanged: (DarkModePref) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedItem by remember(initialTab) { mutableStateOf(initialTab) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Android back press handler
    BackHandler {
        if (onNavigateBackToDashboard != null) {
            onNavigateBackToDashboard()
        } else if (selectedItem != NavigationItem.FUEL) {
            selectedItem = NavigationItem.FUEL
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        com.angelgirlbrand.modiratsokhtandestelam.ui.components.CustomThemedExitDialog(
            onDismissRequest = { showExitDialog = false },
            onConfirmExit = {
                showExitDialog = false
                (context as? Activity)?.finish()
            }
        )
    }

    Scaffold(
        bottomBar = {
            ModernFloatingNavigationBar(
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth > 600.dp
            val contentModifier = Modifier.fillMaxSize()

            when (selectedItem) {
                NavigationItem.DASHBOARD -> Column(modifier = contentModifier) {
                SmartNotificationDashboardScreen(
                    fuelViewModel = fuelViewModel,
                    baleViewModel = baleViewModel,
                    onNavigateToInsurance = { selectedItem = NavigationItem.SERVICES },
                    onNavigateToFuel = { selectedItem = NavigationItem.FUEL },
                    onNavigateToInquiry = { selectedItem = NavigationItem.INQUIRY },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
            NavigationItem.FUEL -> Column(modifier = contentModifier) {
                SectionIntroGuideCard(
                    sectionKey = "fuel_management_tab",
                    title = "راهنمای مدیریت سوخت و باک",
                    description = "در این بخش می‌توانید اطلاعات باک، میزان مصرف در ۱۰۰ کیلومتر و تاریخچه سوخت‌گیری‌های خودرو را ثبت و پایش کنید.",
                    tips = listOf(
                        "با ثبت سوخت‌گیری جدید، درصد باک و شعاع پیمایش خودرو بروزرسانی می‌شود.",
                        "نمودار دونات وضعیت باک را با دقت اعشاری و هشدار وضعیت رزرو نمایش می‌دهد."
                    ),
                    icon = Icons.Default.LocalGasStation,
                    accentColor = Color(0xFF0284C7),
                    securityManager = securityManager,
                    modifier = Modifier.padding(horizontal = if (isWideScreen) 32.dp else 16.dp, vertical = 4.dp)
                )
                FuelManagementScreen(
                    fuelViewModel = fuelViewModel,
                    baleViewModel = baleViewModel,
                    onRequestInsuranceInBale = {
                        selectedItem = NavigationItem.SERVICES
                    },
                    onNavigateToReminders = {
                        selectedItem = NavigationItem.REMINDERS
                    },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
            NavigationItem.SERVICES -> Column(modifier = contentModifier) {
                SectionIntroGuideCard(
                    sectionKey = "services_request_tab",
                    title = "راهنمای درخواست خدمات و بیمه",
                    description = "ثبت انواع درخواست‌های کارت سوخت المثنی، بیمه‌نامه شخص ثالث و بدنه با ارسال مستقیم به پشتیبانی و کارشناس.",
                    tips = listOf(
                        "نوع خدمت مورد نظر را انتخاب و مشخصات وسیله نقلیه را وارد کنید.",
                        "وضعیت تاییدیه و صدور به صورت سه‌رنگ لحظه‌ای قابل پیگیری است."
                    ),
                    icon = Icons.Default.Assignment,
                    accentColor = Color(0xFF059669),
                    securityManager = securityManager,
                    modifier = Modifier.padding(horizontal = if (isWideScreen) 32.dp else 16.dp, vertical = 4.dp)
                )
                BaleBotRequestScreen(
                    baleViewModel = baleViewModel,
                    fuelViewModel = fuelViewModel,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
            NavigationItem.TARIFFS -> Column(modifier = contentModifier) {
                SectionIntroGuideCard(
                    sectionKey = "tariffs_price_tab",
                    title = "راهنمای تعرفه خدمات و هزینه انجام کار",
                    description = "مشاهده کلیه نرخ‌ها و هزینه‌های مصوب تعمیرات، سرویس، بیمه و استعلام‌ها با امکان بروزرسانی زنده برخط.",
                    tips = listOf(
                        "تمامی مبالغ و توضیحات به صورت زنده و مستقیم از سرور و ربات بله همگام‌سازی می‌شوند.",
                        "امکان جستجوی سریع در نام خدمات و فیلتر کردن بر اساس دسته‌بندی وجود دارد."
                    ),
                    icon = Icons.Default.Payments,
                    accentColor = Color(0xFF059669),
                    securityManager = securityManager,
                    modifier = Modifier.padding(horizontal = if (isWideScreen) 32.dp else 16.dp, vertical = 4.dp)
                )
                PriceTariffScreen(
                    baleViewModel = baleViewModel,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
            NavigationItem.INQUIRY -> Column(modifier = contentModifier) {
                SectionIntroGuideCard(
                    sectionKey = "inquiry_payment_tab",
                    title = "راهنمای استعلام و پرداخت سوابق",
                    description = "استعلام زنده خلافی راهور، عوارض آزادراهی و شهرداری با امکان تسویه آنی و مشاهده رسید دیجیتال.",
                    tips = listOf(
                        "استعلام خلافی در این بخش فقط با شماره پلاک و کد ملی مالک انجام می‌پذیرد.",
                        "پس از استعلام، امکان اشتراک‌گذاری رسید رسمی برای شما فراهم است."
                    ),
                    icon = Icons.Default.Search,
                    accentColor = Color(0xFF0284C7),
                    securityManager = securityManager,
                    modifier = Modifier.padding(horizontal = if (isWideScreen) 32.dp else 16.dp, vertical = 4.dp)
                )
                InquiryAndPaymentScreen(
                    baleViewModel = baleViewModel,
                    fuelViewModel = fuelViewModel,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
            NavigationItem.REMINDERS -> Column(modifier = contentModifier) {
                SectionIntroGuideCard(
                    sectionKey = "service_reminders_tab",
                    title = "راهنمای یادآورهای سرویس دوره‌ای",
                    description = "مدیریت سرویس‌های گذشته و آینده شامل تعویض روغن، فیلتر هوا، لنت، تسمه تایم و معاینه فنی.",
                    tips = listOf(
                        "کیلومتر کارکرد هدف یا تاریخ سررسید را ثبت نمایید.",
                        "پس از انجام سرویس، وضعیت آن را به انجام‌شده تغییر دهید."
                    ),
                    icon = Icons.Default.Build,
                    accentColor = Color(0xFF8B5CF6),
                    securityManager = securityManager,
                    modifier = Modifier.padding(horizontal = if (isWideScreen) 32.dp else 16.dp, vertical = 4.dp)
                )
                ServiceRemindersScreen(
                    fuelViewModel = fuelViewModel,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
            NavigationItem.SETTINGS -> Column(modifier = contentModifier) {
                SectionIntroGuideCard(
                    sectionKey = "settings_tab",
                    title = "راهنمای تنظیمات و شخصی‌سازی",
                    description = "تنظیم تم رنگی، حالت تاریک/روشن، امنیت رمز عبور و بازنشانی تور راهنماهای برنامه.",
                    tips = listOf(
                        "امکان انتخاب تم رنگی دلخواه و حالت شب/روز اتوماتیک.",
                        "امکان فعال‌سازی قفل پین‌کد یا اثرانگشت برای ورود امن به برنامه.",
                        "امکان مشاهده و بازنشانی مجدد راهنماهای بخش‌ها از این صفحه."
                    ),
                    icon = Icons.Default.Settings,
                    accentColor = Color(0xFF64748B),
                    securityManager = securityManager,
                    modifier = Modifier.padding(horizontal = if (isWideScreen) 32.dp else 16.dp, vertical = 4.dp)
                )
                SettingsScreen(
                    securityManager = securityManager,
                    baleViewModel = baleViewModel,
                    fuelViewModel = fuelViewModel,
                    currentThemeColor = currentThemeColor,
                    currentDarkModePref = currentDarkModePref,
                    onThemeColorChanged = onThemeColorChanged,
                    onDarkModePrefChanged = onDarkModePrefChanged,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
        }
    }
}
}

@Composable
fun ModernFloatingNavigationBar(
    selectedItem: NavigationItem,
    onItemSelected: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val navWidth = maxWidth
        val showLabels = navWidth > 420.dp
        val isVerySmall = navWidth < 360.dp

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isVerySmall) 8.dp else 14.dp, vertical = 10.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                )
                .clip(RoundedCornerShape(26.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(26.dp)
                ),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationItem.values().forEach { item ->
                    val isSelected = selectedItem == item
                    val interactionSource = remember { MutableInteractionSource() }

                    val animatedScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label = "nav_scale"
                    )

                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "nav_bg"
                    )

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "nav_icon_color"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .scale(animatedScale)
                            .clip(RoundedCornerShape(18.dp))
                            .background(backgroundColor)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onItemSelected(item)
                            }
                            .padding(vertical = 6.dp, horizontal = 2.dp)
                            .testTag("nav_item_${item.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (showLabels) 28.dp else 32.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = iconColor,
                                    modifier = Modifier.size(if (showLabels) 18.dp else 22.dp)
                                )
                            }

                            if (showLabels || isSelected) {
                                Spacer(modifier = Modifier.height(1.dp))

                                Text(
                                    text = item.title,
                                    fontSize = if (isSelected) 8.5.sp else 8.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = iconColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Glowing bottom indicator dot for active item
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
