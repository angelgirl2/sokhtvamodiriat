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
import androidx.compose.material.icons.filled.PieChart
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity
import com.angelgirlbrand.modiratsokhtandestelam.util.PersianDateHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class MonthlyFuelExpenseStat(
    val monthKey: String,
    val monthName: String,
    val totalCost: Long,
    val totalLiters: Double,
    val logCount: Int,
    val percentage: Float,
    val color: Color
)

private val PieChartColors = listOf(
    Color(0xFF0284C7), // Sky Blue (Primary)
    Color(0xFF38BDF8), // Light Sky Blue
    Color(0xFF0D9488), // Teal
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFFF97316), // Orange
    Color(0xFFEF4444), // Red
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEC4899), // Pink
    Color(0xFF6366F1), // Indigo
    Color(0xFF14B8A6), // Cyan
    Color(0xFF84CC16)  // Lime
)

// Persian month names mapping
private fun getPersianMonthName(calendarMonthIndex: Int): String {
    return when (calendarMonthIndex) {
        0 -> "فروردین (ژانویه)"
        1 -> "اردیبهشت (فوریه)"
        2 -> "خرداد (مارس)"
        3 -> "تیر (آوریل)"
        4 -> "مرداد (مه)"
        5 -> "شهریور (ژوئن)"
        6 -> "مهر (ژوئیه)"
        7 -> "آبان (اوت)"
        8 -> "آذر (سپتامبر)"
        9 -> "دی (اکتبر)"
        10 -> "بهمن (نوامبر)"
        11 -> "اسفند (دسامبر)"
        else -> "ماه ${calendarMonthIndex + 1}"
    }
}

@Composable
fun FuelConsumptionChart(
    logs: List<FuelLogEntity>,
    modifier: Modifier = Modifier
) {
    var chartMode by remember { mutableStateOf(0) } // 0: Recharts-style Pie/Donut Chart, 1: Bar Chart
    var selectedSliceIndex by remember { mutableStateOf<Int?>(null) }

    // Animation progress
    var animationTriggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "chart_anim"
    )

    LaunchedEffect(logs) {
        animationTriggered = false
        selectedSliceIndex = null
        kotlinx.coroutines.delay(50)
        animationTriggered = true
    }

    // Process logs to compute monthly expenses
    val (monthlyStats, totalYearCost, totalYearLiters) = remember(logs) {
        val totalCostSum = logs.sumOf { it.totalCost }
        val totalLitersSum = logs.sumOf { it.liters }

        val groupedMap = logs.groupBy { log ->
            val pDate = PersianDateHelper.getPersianDate(log.dateMillis)
            Pair("${pDate.year}-${pDate.month}", "${pDate.monthName} ${pDate.year}")
        }

        val stats = groupedMap.entries.mapIndexed { index, entry ->
            val monthKey = entry.key.first
            val monthName = entry.key.second
            val monthLogs = entry.value
            val costSum = monthLogs.sumOf { it.totalCost }
            val litersSum = monthLogs.sumOf { it.liters }
            val percentage = if (totalCostSum > 0) (costSum.toFloat() / totalCostSum.toFloat()) * 100f else 0f
            val color = PieChartColors[index % PieChartColors.size]

            MonthlyFuelExpenseStat(
                monthKey = monthKey,
                monthName = monthName,
                totalCost = costSum,
                totalLiters = litersSum,
                logCount = monthLogs.size,
                percentage = percentage,
                color = color
            )
        }.sortedByDescending { it.totalCost }

        Triple(stats, totalCostSum, totalLitersSum)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fuel_consumption_chart_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Chart Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تحلیل هزینه سوخت‌گیری‌ها",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "میانگین ماهانه: %,d تومان".format(if (monthlyStats.isNotEmpty()) totalYearCost / monthlyStats.size else 0),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }

                // Switcher Pill (Pie vs Bar)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.padding(2.dp)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        IconButton(
                            onClick = { chartMode = 0 },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (chartMode == 0) Color(0xFF0284C7) else Color.Transparent)
                        ) {
                            Icon(
                                Icons.Default.PieChart,
                                contentDescription = "نمودار دایره‌ای",
                                tint = if (chartMode == 0) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { chartMode = 1 },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (chartMode == 1) Color(0xFF0284C7) else Color.Transparent)
                        ) {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = "نمودار میله‌ای",
                                tint = if (chartMode == 1) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (logs.isEmpty()) {
                // Empty state - Dark Styled
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PieChart,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8).copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "پس از ثبت اولین سوخت‌گیری، نمودار دایره‌ای هزینه‌ها نمایش داده می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                if (chartMode == 0) {
                    // ================= RECHARTS-STYLE PIE / DONUT CHART =================
                    val selectedStat = if (selectedSliceIndex != null && selectedSliceIndex!! in monthlyStats.indices) {
                        monthlyStats[selectedSliceIndex!!]
                    } else null

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(210.dp)
                                .pointerInput(monthlyStats) {
                                    detectTapGestures { offset ->
                                        val center = Offset(size.width / 2f, size.height / 2f)
                                        val dx = offset.x - center.x
                                        val dy = offset.y - center.y
                                        val distance = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                                        val outerRadius = size.width / 2f
                                        val innerRadius = outerRadius * 0.55f

                                        if (distance in innerRadius..outerRadius) {
                                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                            if (angle < 0) angle += 360f

                                            var currentAngle = -90f
                                            if (currentAngle < 0) currentAngle += 360f

                                            var accumulated = 0f
                                            var foundIndex: Int? = null
                                            for (i in monthlyStats.indices) {
                                                val sliceSweep = (monthlyStats[i].percentage / 100f) * 360f
                                                val start = (accumulated - 90f + 360f) % 360f
                                                val end = (start + sliceSweep) % 360f

                                                val inSlice = if (start <= end) {
                                                    angle >= start && angle <= end
                                                } else {
                                                    angle >= start || angle <= end
                                                }

                                                if (inSlice) {
                                                    foundIndex = i
                                                    break
                                                }
                                                accumulated += sliceSweep
                                            }

                                            selectedSliceIndex = if (selectedSliceIndex == foundIndex) null else foundIndex
                                        } else {
                                            selectedSliceIndex = null
                                        }
                                    }
                                }
                        ) {
                            val strokeWidth = 32.dp.toPx()
                            val arcSize = size.width - strokeWidth
                            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                            var startAngle = -90f

                            monthlyStats.forEachIndexed { index, stat ->
                                val sweepAngle = (stat.percentage / 100f) * 360f * animatedProgress
                                val isSelected = selectedSliceIndex == index
                                val currentStroke = if (isSelected) strokeWidth + 8.dp.toPx() else strokeWidth

                                drawArc(
                                    color = stat.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle - 2f, // slice gap
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = currentStroke, cap = StrokeCap.Round)
                                )
                                startAngle += sweepAngle
                            }
                        }

                        // Center Donut Hole Content
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            if (selectedStat != null) {
                                Text(
                                    text = selectedStat.monthName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = selectedStat.color
                                )
                                Text(
                                    text = "%,d".format(selectedStat.totalCost),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "تومان (%.1f%%)".format(selectedStat.percentage),
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            } else {
                                Text(
                                    text = "مجموع کل هزینه",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "%,d".format(totalYearCost),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    text = "تومان (${monthlyStats.size} ماه)",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    // Legend and monthly breakdown chips
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "تفکیک ماه‌ها و درصد سهم از کل بودجه سوخت:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        monthlyStats.forEachIndexed { index, stat ->
                            val isSelected = selectedSliceIndex == index
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedSliceIndex = if (selectedSliceIndex == index) null else index
                                    }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) stat.color else Color(0xFF334155),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                color = if (isSelected) stat.color.copy(alpha = 0.18f) else Color(0xFF1E293B)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(stat.color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stat.monthName,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${stat.logCount} باک)",
                                            fontSize = 9.5.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "%,d تومان".format(stat.totalCost),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) stat.color else Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = stat.color.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "%.1f%%".format(stat.percentage),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = stat.color,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ================= BAR CHART VIEW =================
                    val maxLiters = remember(monthlyStats) {
                        (monthlyStats.maxOfOrNull { it.totalLiters } ?: 10.0).coerceAtLeast(20.0)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val barSpacing = width / (monthlyStats.size.coerceAtLeast(1) * 1.5f)
                            val barWidth = barSpacing * 0.75f
                            val startX = (width - (monthlyStats.size * (barWidth + barSpacing) - barSpacing)) / 2f

                            // Guide lines
                            val steps = 3
                            for (i in 0..steps) {
                                val y = height * (i.toFloat() / steps)
                                drawLine(
                                    color = Color(0xFF334155).copy(alpha = 0.5f),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Bars
                            monthlyStats.forEachIndexed { i, stat ->
                                val x = startX + i * (barWidth + barSpacing)
                                val barHeight = ((stat.totalLiters / maxLiters) * (height - 35.dp.toPx())).toFloat() * animatedProgress
                                val topY = height - barHeight - 12.dp.toPx()

                                val isSelected = selectedSliceIndex == i
                                val barColor = if (isSelected) Color(0xFFF59E0B) else stat.color

                                // Background Slot
                                drawRoundRect(
                                    color = Color(0xFF1E293B),
                                    topLeft = Offset(x, 10.dp.toPx()),
                                    size = Size(barWidth, height - 35.dp.toPx()),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )

                                // Data Bar
                                if (barHeight > 0) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(barColor, barColor.copy(alpha = 0.65f))
                                        ),
                                        topLeft = Offset(x, topY),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )
                                }
                            }
                        }

                        // Labels
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            monthlyStats.forEachIndexed { index, stat ->
                                Text(
                                    text = stat.monthName.take(6),
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedSliceIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSliceIndex == index) Color(0xFF38BDF8) else Color(0xFFCBD5E1),
                                    modifier = Modifier.clickable {
                                        selectedSliceIndex = if (selectedSliceIndex == index) null else index
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
