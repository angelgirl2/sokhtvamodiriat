package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.InquiryRecordEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceRequestEntity
import com.angelgirlbrand.modiratsokhtandestelam.util.PersianDateHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LiveRequestStatusWidget(
    serviceRequests: List<ServiceRequestEntity>,
    inquiries: List<InquiryRecordEntity>,
    onNavigateToServices: () -> Unit,
    onNavigateToInquiries: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine the latest request
    val latestRequest = remember(serviceRequests, inquiries) {
        val req = serviceRequests.maxByOrNull { it.submissionDateMillis }
        val inq = inquiries.maxByOrNull { it.dateMillis }
        if (req != null && inq != null) {
            if (req.submissionDateMillis >= inq.dateMillis) {
                CombinedRequestData(
                    title = req.title,
                    type = req.requestType,
                    status = req.status,
                    dateMillis = req.submissionDateMillis,
                    plate = req.vehiclePlate,
                    isServiceRequest = true
                )
            } else {
                CombinedRequestData(
                    title = inq.title,
                    type = inq.inquiryType,
                    status = inq.status,
                    dateMillis = inq.dateMillis,
                    plate = inq.plateNumber,
                    isServiceRequest = false
                )
            }
        } else if (req != null) {
            CombinedRequestData(
                title = req.title,
                type = req.requestType,
                status = req.status,
                dateMillis = req.submissionDateMillis,
                plate = req.vehiclePlate,
                isServiceRequest = true
            )
        } else if (inq != null) {
            CombinedRequestData(
                title = inq.title,
                type = inq.inquiryType,
                status = inq.status,
                dateMillis = inq.dateMillis,
                plate = inq.plateNumber,
                isServiceRequest = false
            )
        } else {
            null
        }
    }

    // Dynamic Live Clock Ticker for active monitoring state
    var liveTimeText by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        while (true) {
            liveTimeText = sdf.format(Date())
            delay(1000)
        }
    }

    // Gentle pulsing ring animation for states
    val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
    
    // Multi-color pulse for "Checking" state
    val pulseColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF10B981), // Green
        targetValue = Color(0xFFF59E0B), // Orange
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                Color(0xFF38BDF8) at 1500 // Blue at 50%
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseColor"
    )

    // Intermediate color for blue phase
    val checkingColor = if (latestRequest != null && !(latestRequest.status.contains("تایید") || latestRequest.status.contains("پرداخت") || latestRequest.status.contains("رد"))) {
        pulseColor
    } else {
        null
    }

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val scaleSize by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleSize"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_request_status_widget"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(
            1.2.dp,
            if (latestRequest == null) Color(0xFF10B981).copy(alpha = 0.4f)
            else when {
                latestRequest.status.contains("تایید") || latestRequest.status.contains("پرداخت") -> Color(0xFF10B981).copy(alpha = 0.6f)
                latestRequest.status.contains("رد") -> Color(0xFFEF4444).copy(alpha = 0.6f)
                else -> pulseColor.copy(alpha = 0.6f) // Pulsing border for pending
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Widget Title Header Row
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (latestRequest == null) Color(0xFF10B981)
                                else when {
                                    latestRequest.status.contains("تایید") || latestRequest.status.contains("پرداخت") -> Color(0xFF10B981)
                                    latestRequest.status.contains("رد") -> Color(0xFFEF4444)
                                    else -> pulseColor
                                }
                            )
                    ) {
                        // Pulsing outer radar circle
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scaleSize
                                    scaleY = scaleSize
                                    alpha = pulseAlpha
                                }
                                .clip(CircleShape)
                                .background(
                                    if (latestRequest == null) Color(0xFF10B981).copy(alpha = 0.4f)
                                    else when {
                                        latestRequest.status.contains("تایید") || latestRequest.status.contains("پرداخت") -> Color(0xFF10B981).copy(alpha = 0.4f)
                                        latestRequest.status.contains("رد") -> Color(0xFFEF4444).copy(alpha = 0.4f)
                                        else -> pulseColor.copy(alpha = 0.4f)
                                    }
                                )
                        )
                    }

                    Text(
                        text = "مرکز نظارت و پیگیری زنده درخواست‌ها",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Live ticking clock as a premium security feedback
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(0.8.dp, Color(0xFF334155))
                ) {
                    Text(
                        text = "پایش زنده: $liveTimeText",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF334155))

            // Body Section: Animated visibility switching based on state
            AnimatedContent(
                targetState = latestRequest,
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(250))
                },
                label = "statusContentChange"
            ) { req ->
                if (req == null) {
                    // Empty/Default Active Monitoring state
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToServices() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تمام سامانه‌ها پایدار و فعال هستند ✅",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF34D399)
                            )
                            Text(
                                text = "هیچ درخواست فعالی در انتظار نیست. برای ثبت بیمه‌نامه یا عوارض کلیک کنید.",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    // Display latest request status with details and a clickable action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (req.isServiceRequest) onNavigateToServices()
                                else onNavigateToInquiries()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                req.status.contains("تایید") || req.status.contains("پرداخت") -> Color(0xFF10B981).copy(alpha = 0.15f)
                                req.status.contains("رد") -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    when {
                                        req.status.contains("تایید") || req.status.contains("پرداخت") -> Icons.Default.CheckCircle
                                        req.status.contains("رد") -> Icons.Default.ErrorOutline
                                        else -> Icons.Default.HourglassTop
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        req.status.contains("تایید") || req.status.contains("پرداخت") -> Color(0xFF34D399)
                                        req.status.contains("رد") -> Color(0xFFF87171)
                                        else -> Color(0xFFFBBF24)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Text(
                                        text = req.type,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "پلاک: ${req.plate}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = req.title,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFCBD5E1)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "آخرین وضعیت: ${req.status}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        req.status.contains("تایید") || req.status.contains("پرداخت") -> Color(0xFF34D399)
                                        req.status.contains("رد") -> Color(0xFFF87171)
                                        else -> Color(0xFFFBBF24)
                                    }
                                )
                                Text(
                                    text = "• ${PersianDateHelper.toPersianDate(req.dateMillis)}",
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class CombinedRequestData(
    val title: String,
    val type: String,
    val status: String,
    val dateMillis: Long,
    val plate: String,
    val isServiceRequest: Boolean
)
