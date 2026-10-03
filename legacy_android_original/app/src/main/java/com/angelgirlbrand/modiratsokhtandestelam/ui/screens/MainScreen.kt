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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.security.AppThemeColor
import com.angelgirlbrand.modiratsokhtandestelam.security.DarkModePref
import com.angelgirlbrand.modiratsokhtandestelam.security.SecurityManager
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel

enum class NavigationItem(
    val title: String,
    val icon: ImageVector
) {
    FUEL("مدیریت سوخت", Icons.Default.LocalGasStation),
    SERVICES("درخواست خدمات", Icons.Default.Assignment),
    INQUIRY("استعلام", Icons.Default.Search),
    REMINDERS("سرویس و نگهداری", Icons.Default.Build),
    SETTINGS("تنظیمات", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    fuelViewModel: FuelViewModel,
    baleViewModel: BaleServiceViewModel,
    securityManager: SecurityManager,
    currentThemeColor: AppThemeColor = AppThemeColor.SKY_BLUE,
    currentDarkModePref: DarkModePref = DarkModePref.SYSTEM,
    onThemeColorChanged: (AppThemeColor) -> Unit = {},
    onDarkModePrefChanged: (DarkModePref) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf(NavigationItem.FUEL) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Android back press handler
    BackHandler {
        if (selectedItem != NavigationItem.FUEL) {
            selectedItem = NavigationItem.FUEL
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "خروج از برنامه",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "آیا اطمینان دارید که می‌خواهید از برنامه مدیریت سوخت خارج شوید؟",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        (context as? Activity)?.finish()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("خروج")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("انصراف")
                }
            },
            shape = RoundedCornerShape(20.dp)
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
        val modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (selectedItem) {
            NavigationItem.FUEL -> FuelManagementScreen(
                fuelViewModel = fuelViewModel,
                onRequestInsuranceInBale = {
                    selectedItem = NavigationItem.SERVICES
                },
                onNavigateToReminders = {
                    selectedItem = NavigationItem.REMINDERS
                },
                modifier = modifier
            )
            NavigationItem.SERVICES -> BaleBotRequestScreen(
                baleViewModel = baleViewModel,
                fuelViewModel = fuelViewModel,
                modifier = modifier
            )
            NavigationItem.INQUIRY -> InquiryAndPaymentScreen(
                baleViewModel = baleViewModel,
                fuelViewModel = fuelViewModel,
                modifier = modifier
            )
            NavigationItem.REMINDERS -> ServiceRemindersScreen(
                fuelViewModel = fuelViewModel,
                modifier = modifier
            )
            NavigationItem.SETTINGS -> SettingsScreen(
                securityManager = securityManager,
                baleViewModel = baleViewModel,
                fuelViewModel = fuelViewModel,
                currentThemeColor = currentThemeColor,
                currentDarkModePref = currentDarkModePref,
                onThemeColorChanged = onThemeColorChanged,
                onDarkModePrefChanged = onDarkModePrefChanged,
                modifier = modifier
            )
        }
    }
}

@Composable
fun ModernFloatingNavigationBar(
    selectedItem: NavigationItem,
    onItemSelected: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = item.title,
                            fontSize = if (isSelected) 9.5.sp else 9.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = iconColor,
                            maxLines = 1
                        )

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
