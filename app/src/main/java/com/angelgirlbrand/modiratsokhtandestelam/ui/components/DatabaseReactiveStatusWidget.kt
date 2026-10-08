package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.InquiryRecordEntity
import com.angelgirlbrand.modiratsokhtandestelam.util.PersianDateHelper

data class StatusSummaryItem(
    val title: String,
    val count: Int,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun DatabaseReactiveStatusWidget(
    inquiries: List<InquiryRecordEntity>,
    modifier: Modifier = Modifier
) {
    val totalCount = inquiries.size
    val approvedCount = inquiries.count { it.isApproved }
    val rejectedCount = inquiries.count { it.isRejected }
    val pendingCount = inquiries.count { it.isPending }

    val statusItems = listOf(
        StatusSummaryItem("کل استعلام‌ها", totalCount, Icons.Default.ListAlt, MaterialTheme.colorScheme.primary),
        StatusSummaryItem("تایید شده", approvedCount, Icons.Default.CheckCircle, Color(0xFF10B981)),
        StatusSummaryItem("در حال بررسی", pendingCount, Icons.Default.HourglassEmpty, Color(0xFFF59E0B)),
        StatusSummaryItem("رد شده", rejectedCount, Icons.Default.Cancel, Color(0xFFEF4444))
    )

    val latestInquiry = inquiries.maxByOrNull { it.dateMillis }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("database_reactive_status_widget"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "داشبورد زنده پایگاه داده",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = BorderStroke(0.6.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "متصل",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // LazyRow for Status Summary Cards with Dedicated Icons - Compact
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 1.dp)
                ) {
                    items(statusItems) { item ->
                        ReactiveStatusLazyCard(
                            title = item.title,
                            count = item.count.toString(),
                            icon = item.icon,
                            color = item.color
                        )
                    }
                }

                // Latest Inquiry Live Status Banner - Compact
                if (latestInquiry != null) {
                    val isApproved = latestInquiry.isApproved
                    val isRejected = latestInquiry.isRejected
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            isApproved -> Color(0xFF10B981).copy(alpha = 0.12f)
                            isRejected -> Color(0xFFEF4444).copy(alpha = 0.12f)
                            else -> Color(0xFFF59E0B).copy(alpha = 0.12f)
                        },
                        border = BorderStroke(
                            0.8.dp,
                            when {
                                isApproved -> Color(0xFF10B981).copy(alpha = 0.4f)
                                isRejected -> Color(0xFFEF4444).copy(alpha = 0.4f)
                                else -> Color(0xFFF59E0B).copy(alpha = 0.4f)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "آخرین استعلام (${latestInquiry.plateNumber}):",
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = latestInquiry.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    isApproved -> Color(0xFF10B981)
                                    isRejected -> Color(0xFFEF4444)
                                    else -> Color(0xFFF59E0B)
                                }
                            ) {
                                Text(
                                    text = if (isApproved) "تایید شده" else if (isRejected) "رد شده" else latestInquiry.status,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
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

@Composable
private fun ReactiveStatusLazyCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(96.dp),
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.2f),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Text(
                    text = count,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }
            Text(
                text = title,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
