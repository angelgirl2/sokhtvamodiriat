package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.security.PriceCategory
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.TariffTableItem

/**
 * Unified, consistent Material 3 component for displaying inquiries & insurance tariffs.
 * Real-time in-place updates for both price text and description with smooth animations.
 */
@Composable
fun DynamicServiceTariffDataTable(
    items: List<TariffTableItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dynamic_service_tariff_unified_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "موردی با این مشخصات یافت نشد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "عبارت جستجو را تغییر داده یا فیلتر را بازنشانی فرمایید.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items.forEachIndexed { index, item ->
                UnifiedTariffItemRow(
                    index = index + 1,
                    item = item
                )
            }
        }
    }
}

@Composable
private fun UnifiedTariffItemRow(
    index: Int,
    item: TariffTableItem
) {
    val borderColor = if (item.isRecentlyUpdated) {
        Color(0xFF10B981)
    } else {
        Color(0xFF334155).copy(alpha = 0.8f)
    }

    val cardBg = if (item.isRecentlyUpdated) {
        Color(0xFF064E3B).copy(alpha = 0.3f)
    } else {
        Color(0xFF0F172A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tariff_item_${item.key}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(if (item.isRecentlyUpdated) 1.5.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Index, Service Name, Category, Price Badge
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
                    // Index badge
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E293B),
                        border = BorderStroke(0.8.dp, Color(0xFF475569)),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$index",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (item.category == PriceCategory.INSURANCE) Icons.Default.Security else Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = if (item.category == PriceCategory.INSURANCE) Color(0xFF38BDF8) else Color(0xFF34D399),
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = item.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp,
                                color = Color.White
                            )
                        }

                        // Category Pill
                        CategoryPill(category = item.category)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Price / Cost Badge (Animated on real-time text updates)
                AnimatedContent(
                    targetState = item.priceText,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) + scaleIn()) togetherWith (fadeOut(animationSpec = tween(200)))
                    },
                    label = "price_change_anim"
                ) { targetPrice ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF064E3B),
                        border = BorderStroke(1.2.dp, Color(0xFF10B981))
                    ) {
                        Text(
                            text = targetPrice,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.5.sp,
                            color = Color(0xFFA7F3D0),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Row 2: Live In-place Description (Animated on real-time description updates)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = item.description,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
                    },
                    label = "desc_change_anim"
                ) { targetDesc ->
                    Text(
                        text = targetDesc,
                        fontSize = 11.5.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.5.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Row 3: Online status footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = "سامانه استعلام و صدور آنلاین فعال",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6EE7B7)
                    )
                }

                if (item.isRecentlyUpdated) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF059669)
                    ) {
                        Text(
                            text = "بروزرسانی شد ✓",
                            fontSize = 9.5.sp,
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

@Composable
private fun CategoryPill(category: PriceCategory) {
    val (bgColor, textColor, label) = when (category) {
        PriceCategory.INSURANCE -> Triple(Color(0xFF0369A1).copy(alpha = 0.3f), Color(0xFF7DD3FC), "خرید و صدور انواع بیمه")
        PriceCategory.INQUIRY -> Triple(Color(0xFF065F46).copy(alpha = 0.35f), Color(0xFF6EE7B7), "استعلام‌ها و خلافی خودرو")
        PriceCategory.ALL -> Triple(Color(0xFF334155), Color(0xFFE2E8F0), "همه خدمات")
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1
        )
    }
}
