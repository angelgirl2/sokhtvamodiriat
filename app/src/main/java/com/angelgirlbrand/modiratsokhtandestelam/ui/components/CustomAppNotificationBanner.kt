package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * CustomAppNotificationBanner:
 * Replaces default system Toasts (with plain white/light backgrounds) with a custom,
 * dark-themed, elevated floating banner that appears at the top of the screen matching the app's design.
 */
@Composable
fun CustomAppNotificationBanner(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.NotificationsActive,
    accentColor: Color = Color(0xFF38BDF8),
    autoDismissDurationMillis: Long = 4000L
) {
    var isVisible by remember(message) { mutableStateOf(!message.isNullOrBlank()) }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            isVisible = true
            delay(autoDismissDurationMillis)
            isVisible = false
            delay(300)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible && !message.isNullOrBlank(),
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.95f),
                    border = BorderStroke(
                        1.2.dp,
                        Brush.horizontalGradient(
                            listOf(
                                accentColor.copy(alpha = 0.8f),
                                Color(0xFF0284C7).copy(alpha = 0.4f),
                                accentColor.copy(alpha = 0.6f)
                            )
                        )
                    ),
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth(0.96f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = accentColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "اعلان سیستم مدیریت و استعلام",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = message ?: "",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                isVisible = false
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
