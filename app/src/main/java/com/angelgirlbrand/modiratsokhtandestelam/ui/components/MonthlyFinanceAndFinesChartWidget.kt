package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.InquiryRecordEntity
import com.angelgirlbrand.modiratsokhtandestelam.util.PersianDateHelper
import kotlin.math.max

data class MonthlyFinanceBarData(
    val monthIndex: Int, // 1 to 12
    val monthName: String,
    val fuelExpenseTomans: Long,
    val finesAmountTomans: Long,
    val fuelLiters: Double,
    val finesCount: Int
) {
    val totalExpense: Long get() = fuelExpenseTomans + finesAmountTomans
}

/**
 * Modern Bar Chart Widget inspired by Recharts visual design language.
 * Displays Monthly Fuel Expenses and Traffic Fines History side-by-side with
 * interactive bar selection, dual legends, responsive gridlines, tooltips, and summary cards.
 */
@Composable
fun MonthlyFinanceAndFinesChartWidget(
    fuelLogs: List<FuelLogEntity>,
    inquiries: List<InquiryRecordEntity>,
    modifier: Modifier = Modifier
) {
    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }
    var chartAnimationProgress by remember { mutableFloatStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = chartAnimationProgress,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "recharts_bar_anim"
    )

    LaunchedEffect(Unit) {
        chartAnimationProgress = 1f
    }

    // Prepare monthly aggregated statistics across Persian calendar months
    val monthlyData = remember(fuelLogs, inquiries) {
        val nowPDate = PersianDateHelper.getPersianDate(System.currentTimeMillis())
        val currentYear = nowPDate.year
        val currentMonth = nowPDate.month // 1 to 12

        // Take the last 5-6 months up to current month
        val monthsToShow = mutableListOf<Int>()
        for (offset in 5 downTo 0) {
            var m = currentMonth - offset
            if (m <= 0) m += 12
            monthsToShow.add(m)
        }

        monthsToShow.map { mIndex ->
            val mName = if (mIndex in 1..12) PersianDateHelper.PERSIAN_MONTH_NAMES[mIndex - 1] else "ماه $mIndex"

            val mFuelLogs = fuelLogs.filter { log ->
                val pDate = PersianDateHelper.getPersianDate(log.dateMillis)
                pDate.month == mIndex
            }
            val fuelCost = mFuelLogs.sumOf { it.totalCost }
            val fuelLiters = mFuelLogs.sumOf { it.liters }

            val mInquiries = inquiries.filter { inq ->
                val pDate = PersianDateHelper.getPersianDate(inq.dateMillis)
                pDate.month == mIndex
            }
            val finesCost = mInquiries.sumOf { it.amount }
            val finesCount = mInquiries.size

            MonthlyFinanceBarData(
                monthIndex = mIndex,
                monthName = mName,
                fuelExpenseTomans = fuelCost,
                finesAmountTomans = finesCost,
                fuelLiters = fuelLiters,
                finesCount = finesCount
            )
        }
    }

    val totalFuelExpenseAll = remember(monthlyData) { monthlyData.sumOf { it.fuelExpenseTomans } }
    val totalFinesAmountAll = remember(monthlyData) { monthlyData.sumOf { it.finesAmountTomans } }
    val totalExpenseAll = totalFuelExpenseAll + totalFinesAmountAll

    val maxBarValue = remember(monthlyData) {
        val maxSingle = monthlyData.maxOfOrNull { max(it.fuelExpenseTomans, it.finesAmountTomans) } ?: 0L
        if (maxSingle > 0L) (maxSingle * 1.25f).toLong() else 100_000L
    }

    val selectedData = remember(selectedMonthIndex, monthlyData) {
        monthlyData.firstOrNull { it.monthIndex == selectedMonthIndex }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_finance_fines_chart_widget"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A) // Sleek dark Recharts canvas
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.2.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with badge and title
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
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "نمودار هزینه‌ها و خلافی‌های ماهانه",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF6366F1).copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "Recharts Style",
                                    color = Color(0xFFA5B4FC),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "مقایسه ستونی هزینه سوخت با جریمه‌ها و خلافی‌ها",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Summary Totals strip (Fuel & Fines)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Fuel cost card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF10B981))
                        )
                        Column {
                            Text(
                                text = "هزینه سوخت",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "%,d تومان".format(totalFuelExpenseAll),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399)
                            )
                        }
                    }
                }

                // Fines amount card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFF43F5E))
                        )
                        Column {
                            Text(
                                text = "مجموع خلافی‌ها",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "%,d تومان".format(totalFinesAmountAll),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFB7185)
                            )
                        }
                    }
                }
            }

            // Interactive Tooltip Card on Bar Tap
            AnimatedVisibility(visible = selectedData != null) {
                if (selectedData != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "جزئیات ماه «${selectedData.monthName}»:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    text = "جمع کل هزینه‌ها: %,d تومان".format(selectedData.totalExpense),
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "سوخت: %,d تومان (%.1f لیتر)".format(selectedData.fuelExpenseTomans, selectedData.fuelLiters),
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF34D399)
                                )
                                Text(
                                    text = "خلافی: %,d تومان (%d مورد)".format(selectedData.finesAmountTomans, selectedData.finesCount),
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFFB7185)
                                )
                            }
                        }
                    }
                }
            }

            // RECHARTS-INSPIRED COLUMN CHART CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(monthlyData) {
                            detectTapGestures { offset ->
                                val count = monthlyData.size.coerceAtLeast(1)
                                val groupWidth = size.width / count.toFloat()
                                val clickedIndex = (offset.x / groupWidth).toInt()
                                if (clickedIndex in monthlyData.indices) {
                                    val item = monthlyData[clickedIndex]
                                    selectedMonthIndex = if (selectedMonthIndex == item.monthIndex) null else item.monthIndex
                                }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val bottomLabelArea = 24.dp.toPx()
                    val chartHeight = canvasHeight - bottomLabelArea

                    // Horizontal Grid Lines (Recharts CartesianGrid style)
                    val gridLinesCount = 4
                    for (i in 0..gridLinesCount) {
                        val y = chartHeight * (i.toFloat() / gridLinesCount)
                        drawLine(
                            color = Color(0xFF334155).copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val groupCount = monthlyData.size.coerceAtLeast(1)
                    val groupWidth = canvasWidth / groupCount.toFloat()
                    val barWidth = (groupWidth * 0.32f).coerceIn(8.dp.toPx(), 26.dp.toPx())
                    val barSpacing = 4.dp.toPx()

                    monthlyData.forEachIndexed { index, item ->
                        val groupCenterX = index * groupWidth + (groupWidth / 2f)
                        val isSelected = selectedMonthIndex == item.monthIndex

                        // Selected column highlight column background
                        if (isSelected) {
                            drawRoundRect(
                                color = Color(0xFF38BDF8).copy(alpha = 0.12f),
                                topLeft = Offset(index * groupWidth + 2.dp.toPx(), 0f),
                                size = Size(groupWidth - 4.dp.toPx(), chartHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }

                        // Bar 1: Fuel Expense (Green / Emerald)
                        val fuelFraction = if (maxBarValue > 0) (item.fuelExpenseTomans.toFloat() / maxBarValue).coerceIn(0f, 1f) else 0f
                        val fuelBarHeight = (fuelFraction * chartHeight * animatedProgress).coerceAtLeast(if (item.fuelExpenseTomans > 0) 6.dp.toPx() else 0f)
                        val fuelBarX = groupCenterX - barWidth - (barSpacing / 2f)
                        val fuelBarY = chartHeight - fuelBarHeight

                        if (fuelBarHeight > 0f) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF34D399),
                                        Color(0xFF059669)
                                    )
                                ),
                                topLeft = Offset(fuelBarX, fuelBarY),
                                size = Size(barWidth, fuelBarHeight),
                                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                            )
                        } else {
                            // Subtle placeholder dot for 0 value
                            drawRoundRect(
                                color = Color(0xFF334155).copy(alpha = 0.6f),
                                topLeft = Offset(fuelBarX, chartHeight - 3.dp.toPx()),
                                size = Size(barWidth, 3.dp.toPx()),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }

                        // Bar 2: Traffic Fines (Rose / Red)
                        val finesFraction = if (maxBarValue > 0) (item.finesAmountTomans.toFloat() / maxBarValue).coerceIn(0f, 1f) else 0f
                        val finesBarHeight = (finesFraction * chartHeight * animatedProgress).coerceAtLeast(if (item.finesAmountTomans > 0) 6.dp.toPx() else 0f)
                        val finesBarX = groupCenterX + (barSpacing / 2f)
                        val finesBarY = chartHeight - finesBarHeight

                        if (finesBarHeight > 0f) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFB7185),
                                        Color(0xFFE11D48)
                                    )
                                ),
                                topLeft = Offset(finesBarX, finesBarY),
                                size = Size(barWidth, finesBarHeight),
                                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                            )
                        } else {
                            // Subtle placeholder dot for 0 value
                            drawRoundRect(
                                color = Color(0xFF334155).copy(alpha = 0.6f),
                                topLeft = Offset(finesBarX, chartHeight - 3.dp.toPx()),
                                size = Size(barWidth, 3.dp.toPx()),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                }

                // Month labels along bottom X-axis
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    monthlyData.forEach { item ->
                        val isSelected = selectedMonthIndex == item.monthIndex
                        Text(
                            text = item.monthName.take(6),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable {
                                    selectedMonthIndex = if (selectedMonthIndex == item.monthIndex) null else item.monthIndex
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Legend Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF10B981))
                    )
                    Text("هزینه بنزین و سوخت", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                }

                Spacer(modifier = Modifier.width(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFF43F5E))
                    )
                    Text("جرائم و خلافی‌ها", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                }
            }

            // Empty state helper note when no logs or fines recorded
            if (totalExpenseAll == 0L) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "با ثبت باک‌های جدید و استعلام خلافی‌ها، داده‌های ستونی به صورت زنده بر اساس ماه‌های سال نمایش داده می‌شوند.",
                            fontSize = 10.5.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }
    }
}
